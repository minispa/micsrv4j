package com.github.minispa.micsrv.media.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.minispa.micsrv.media.model.MediaOperate;
import com.github.minispa.micsrv.media.utils.MixAll;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.*;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executor;

import static java.util.concurrent.Executors.newFixedThreadPool;

@Slf4j
@Component
public class ComplexMarkHandler {

    static final String qrContent = "https://developer.aliyun.com/ask/212727?spm=a2c6h.13524658";

    static final String
            ffmpegParentWorkPath = "C:/Users/Mr.Y/Videos/material",
            ffmpeg = "ffmpeg",
            ffprobe = "ffprobe";

    static final double FPS_TAIL = 25D;
    static final int WIDTH = 720, HEIGHT = 1280;

    static ListeningExecutorService taskExecutor = MoreExecutors.listeningDecorator(newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2, new ThreadFactoryBuilder().setNameFormat("media-pool-%d").build()));
    static Executor callbackExecutor = newFixedThreadPool(4, new ThreadFactoryBuilder().setNameFormat("callback-pool-%d").build());

    @Autowired
    private DefaultMQProducer rocketMQProducer;

    public void exec(MediaOperate mediaOperate) {
        log.info("mediaOperate: {}", JSON.toJSONString(mediaOperate));
        final Map<String, String> args = mediaOperate.getArgs();
        try {
            ListenableFuture<Integer> future = taskExecutor.submit(() -> {
                ProcessBuilder processBuilder = new ProcessBuilder(mediaOperate.getCommands());
                String ffmpegWorkPath = null;
                if (MapUtils.isNotEmpty(args)) {
                    ffmpegWorkPath = args.get("ffmpegWorkPath");
                }
                if (StringUtils.isNotBlank(ffmpegWorkPath)) {
                    processBuilder.directory(new File(ffmpegWorkPath));
                }
                Process process = processBuilder.inheritIO().redirectErrorStream(true).start();
                int exitValue = process.waitFor();
                log.info("exec - exitValue: {}, commands: {}", exitValue, String.join(" ", mediaOperate.getCommands()));
                return exitValue;
            });

            Futures.<Integer>addCallback(future, new FutureCallback<Integer>() {
                @Override
                public void onSuccess(@Nullable Integer result) {
                    mediaOperate.setStatus(result);
                    callback(mediaOperate);
                }

                @Override
                public void onFailure(Throwable throwable) {
                    mediaOperate.setStatus(-1);
                    callback(mediaOperate);
                }
            }, callbackExecutor);
        } catch (Exception e) {
            log.error("exec - error", e);
            mediaOperate.setStatus(500);
            callback(mediaOperate);
        }
    }

    public String complexMark(MediaOperate mediaOperate) {
        Map<String, String> args = Maps.newHashMap(mediaOperate.getArgs());
        String origin_input = args.get("originPath");
        log.info("origin_input: {}", origin_input);
        String qr_input = origin_input.replaceAll("\\.\\S{3}$", "_qr.jpg");
        qr_input = createQrCode(args.get("qrCodeText"), 250, 250, qr_input);
        JSONObject streamObj = MixAll.anyVideoStream(getMetadata(origin_input));
        if (MapUtils.isEmpty(streamObj)) {
            log.warn("file: {} not found any video stream.", origin_input);
            return null;
        }
        cropRoundedImage(qr_input);
        log.info("qr_input: {}", qr_input);
        double fps_head = Double.parseDouble(streamObj.getString("r_frame_rate").split("/")[0]), fps_tail = FPS_TAIL;
        if (fps_head > fps_tail) {
            fps_head = fps_tail;
        } else {
            fps_tail = fps_head;
        }
        int width = streamObj.getIntValue("coded_width"), height = streamObj.getIntValue("coded_height");

        String absoluteParentWorkPath = ffmpegParentWorkPath,
                dir = UUID.randomUUID().toString().replace("-", ""),
                ffmpegConcatWorkPath = createDirectoriesIfNested(absoluteParentWorkPath.concat("/").concat(dir));

        Pair<Integer, Integer> scale = MixAll.scale(width, height, WIDTH, HEIGHT);
        String scale_X = scale.getLeft().toString(), scale_Y = scale.getRight().toString(),
                gif_input = absoluteParentWorkPath.concat("/logo.gif"),
                origin_out = ffmpegConcatWorkPath.concat("/origin_out.mp4"),
                origin_out_relative = "origin_out.mp4",
                tail_input = absoluteParentWorkPath.concat("/tail.mp4"),
                tail_out = ffmpegConcatWorkPath.concat("/tail_out.mp4"),
                tail_out_relative = "tail_out.mp4",
                filelist = ffmpegConcatWorkPath.concat("/filelist.txt"),
                final_out = origin_input.replaceAll("\\.\\S{3}$", "_720x1280.mp4");

        List<String> commands = Lists.newArrayList(
                "cmd", "/c",
                ffmpeg, "-y", "-i", origin_input, "-ignore_loop", "0", "-i", gif_input, "-filter_complex", "\"[0]scale=" + scale_X + ":" + scale_Y + "[sv];[sv]pad=720:1280:0:640-ih/2:black,fps=" + fps_head + "[pv];[1]scale=100:118[wm];[pv][wm]overlay=W-w:18:shortest=1\"", origin_out,
                "&&", ffmpeg, "-y", "-i", tail_input, "-i", qr_input, "-filter_complex", "\"[1]scale=250:250[wm];[0]fps=" + fps_tail + "[fv];[fv][wm]overlay=(W-250)/2:200\"", tail_out,
                "&&", "echo", "file", "'" + origin_out_relative + "'", ">", filelist,
                "&&", "echo", "file", "'" + tail_out_relative + "'", ">>", filelist,
                "&&", ffmpeg, "-y", "-safe", "0", "-f", "concat", "-i", filelist, "-c", "copy", final_out
        );

        if (isLinux()) {
            commands = Lists.newArrayList("/bin/bash", "-c",
                    ffmpeg + " -y -i " + origin_input + " -ignore_loop 0 -i " + gif_input + " -filter_complex \"[0]scale=" + scale_X + ":" + scale_Y + "[sv];[sv]pad=720:1280:0:640-ih/2:black,fps=" + fps_head + "[pv];[1]scale=100:118[wm];[pv][wm]overlay=W-w+5:18:shortest=1\" " + origin_out +
                            " && " + ffmpeg + " -y -i " + tail_input + " -i " + qr_input + " -filter_complex \"[1]scale=250:250[wm];[0]fps=" + fps_tail + "[fv];[fv][wm]overlay=(W-250)/2:200\" " + tail_out +
                            " && echo file \'" + origin_out_relative + "\' >" + filelist +
                            " && echo file \'" + tail_out_relative + "\' >> " + filelist +
                            " && " + ffmpeg + " -y -safe 0 -f concat -i " + filelist + " -c copy " + final_out);
        }

        log.info("final_out: {}", final_out);
        args.put("finalPath", final_out);
        args.put("ffmpegWorkPath", ffmpegConcatWorkPath);
        mediaOperate.setCommands(commands);
        mediaOperate.setArgs(args);
        exec(mediaOperate);
        return final_out;
    }

    public String complexLRConcat(MediaOperate mediaOperate) {
        final Map<String, String> args = mediaOperate.getArgs();
        JSONObject backdata = MixAll.anyVideoStream(getMetadata(args.get("backgroundPath"))),
                foredata = MixAll.anyVideoStream(getMetadata(args.get("foregroundPath")));
        int maxX = backdata.getIntValue("coded_width"), maxY = backdata.getIntValue("coded_height");
        Pair<Integer, Integer> scale = MixAll.scale(foredata.getIntValue("coded_width"), foredata.getIntValue("coded_height"), maxX, maxY);

        String final_out = args.get("foregroundPath").replaceAll("\\.\\S{3}$", "_LR.mp4");
        List<String> commands = Lists.newArrayList(ffmpeg, "-y", "-i", args.get("foregroundPath"), "-i", args.get("foregroundPath"),
                "-filter_complex", "\"[0:v]pad=iw+" + scale.getLeft() + "[bp];[1:v]scale=" + scale.getLeft() + ":" + scale.getRight() + "[sv];[bp][sv]overlay=W-" + scale.getLeft() + ":(H-" + scale.getRight() + ")/2\"", final_out);
        exec(mediaOperate);
        return final_out;
    }

    public JSONObject getMetadata(String absolutePath) {
        log.info("getMetadata - absolutePath: {}", absolutePath);
        try {
            String[] commands = {ffprobe, "-show_streams", "-print_format", "json", absolutePath};
            ProcessBuilder processBuilder = new ProcessBuilder(commands);
            Process process = processBuilder.start();
            String metadata = IOUtils.toString(process.getInputStream(), "utf-8");
            String errormsg = IOUtils.toString(process.getErrorStream(), "utf-8");
            log.info("error: {}", errormsg);
            int exitValue = process.waitFor();
            log.info("getMetadata - exitValue: {}, commands: {}", exitValue, String.join(" ", commands));
            return JSON.parseObject(metadata);
        } catch (Exception e) {
            log.error("exec error", e);
            return new JSONObject();
        }
    }

    private static String createDirectoriesIfNested(String absolutePath) {
        Path path = Paths.get(absolutePath);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                Throwables.throwIfUnchecked(e);
            }
        }
        return absolutePath;
    }

    @SneakyThrows
    public void callback(MediaOperate mediaOperate) {
        log.info("callback - mediaOperate: {}", JSON.toJSONString(mediaOperate));
        switch (mediaOperate.getCallbackType()) {
            case RocketMQ:
                Message message = new Message(mediaOperate.getCallback(), JSON.toJSONBytes(mediaOperate));
                message.putUserProperty("_TraceMark_", MDC.get("_TraceMark_"));
                SendResult sendResult = rocketMQProducer.send(message);
                log.info("callback - sendResult: {}", sendResult);
                break;
            default:
        }
    }

    private static BufferedImage cropRoundedCorner(BufferedImage image, int cornerRadius) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage corner = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = corner.createGraphics();
        g2.setComposite(AlphaComposite.Src);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Float(0, 0, width, height, cornerRadius, cornerRadius));

        g2.setComposite(AlphaComposite.SrcAtop);
        g2.drawImage(image, 0, 0, null);
        g2.dispose();

        return corner;
    }

    private static void cropRoundedImage(String filePath) {
        try {
            BufferedImage image = ImageIO.read(new File(filePath));
            image = cropRoundedCorner(image, 30);
            ImageIO.write(image, "jpg", new File(filePath));
        } catch (Exception e) {
            log.error("cropRoundedImage error", e);
        }
    }

    private static String createQrCode(String content, int width, int height, String filePath) {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, 2);

        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);

            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix, new MatrixToImageConfig());
            ImageIO.write(bufferedImage, "jpg", new File(filePath));

        } catch (Exception e) {
            log.error("createQrCode error", e);
        }
        return filePath;
    }

    private static boolean isWindows() {
        String os = System.getProperty("os.name");
        return os.toLowerCase().contains("windows");
    }

    private static boolean isLinux() {
        String os = System.getProperty("os.name");
        return os.toLowerCase().contains("linux");
    }
}
