package com.adminbase.auth;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CaptchaService {

    private static final String CHARS = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final long TTL = 5 * 60_000L;
    private static final Color[] PALETTE = {
            new Color(0x4d7c0f), new Color(0x3b82f6), new Color(0xb45309),
            new Color(0x7c3aed), new Color(0x0f766e)
    };

    private final Map<String, Object[]> store = new ConcurrentHashMap<String, Object[]>();
    private final Random rnd = new Random();

    public Map<String, Object> gen() {
        purge();
        String key = UUID.randomUUID().toString().replace("-", "");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) sb.append(CHARS.charAt(rnd.nextInt(CHARS.length())));
        store.put(key, new Object[]{sb.toString(), System.currentTimeMillis() + TTL});
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("key", key);
        m.put("img", draw(sb.toString()));
        return m;
    }

    public boolean verify(String key, String code) {
        if (key == null || code == null) return false;
        Object[] o = store.remove(key);
        return o != null
                && System.currentTimeMillis() <= (Long) o[1]
                && ((String) o[0]).equalsIgnoreCase(code.trim());
    }

    private void purge() {
        long now = System.currentTimeMillis();
        for (String k : new HashSet<String>(store.keySet())) {
            Object[] o = store.get(k);
            if (o == null || (Long) o[1] < now) store.remove(k);
        }
    }

    private String draw(String code) {
        BufferedImage img = new BufferedImage(130, 42, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(0xf2f4ef));
        g.fillRect(0, 0, 130, 42);
        for (int i = 0; i < 4; i++) {
            g.setColor(new Color(220 + rnd.nextInt(30), 230 + rnd.nextInt(25), 210 + rnd.nextInt(40)));
            g.drawLine(rnd.nextInt(130), rnd.nextInt(42), rnd.nextInt(130), rnd.nextInt(42));
        }
        Font[] fonts = {new Font("SansSerif", Font.BOLD, 30), new Font("Monospaced", Font.BOLD, 28)};
        for (int i = 0; i < code.length(); i++) {
            g.setColor(PALETTE[rnd.nextInt(PALETTE.length)]);
            g.setFont(fonts[rnd.nextInt(fonts.length)]);
            double rot = (rnd.nextDouble() - 0.5) * 0.5;
            int x = 12 + i * 28, y = 30 + rnd.nextInt(6);
            g.rotate(rot, x, y);
            g.drawString(String.valueOf(code.charAt(i)), x, y);
            g.rotate(-rot, x, y);
        }
        for (int i = 0; i < 24; i++) {
            g.setColor(new Color(150 + rnd.nextInt(100), 160 + rnd.nextInt(90), 140 + rnd.nextInt(110)));
            g.fillRect(rnd.nextInt(130), rnd.nextInt(42), 2, 2);
        }
        g.dispose();
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", bos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(bos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("验证码生成失败", e);
        }
    }
}
