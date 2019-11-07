package com.example.sushi;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import java.util.Random;


public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Creation de l'instance image

        BitmapFactory.Options options = new BitmapFactory.Options(); // permet la manip de tous les pixels
        options.inMutable = true;
        final Bitmap img = BitmapFactory.decodeResource(getResources(), R.drawable.sushi, options);

        ImageView iv = findViewById(R.id.sushiImg);
        iv.setImageBitmap(img); // permet de modifier l'image

        ImageView histoR = findViewById(R.id.histoR);
        ImageView histoG = findViewById(R.id.histoG);
        ImageView histoB = findViewById(R.id.histoB);

        final Bitmap hR = Bitmap.createBitmap(256, 256, Bitmap.Config.RGB_565);
        final Bitmap hG = Bitmap.createBitmap(256, 256, Bitmap.Config.RGB_565);
        final Bitmap hB = Bitmap.createBitmap(256, 256, Bitmap.Config.RGB_565);

        histoR.setImageBitmap(hR);
        histoG.setImageBitmap(hG);
        histoB.setImageBitmap(hB);

        final int w = img.getWidth();
        final int h = img.getHeight();

        TextView tv = findViewById(R.id.sizeImg); // affiche la taille de l'image (cherche l'id)
        tv.setText(w + "*" + h);

        final int[] mon_image = new int[w * h];
        img.getPixels(mon_image, 0, w, 0, 0, w, h);

        final Button menuBtn = findViewById(R.id.menu);
        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(MainActivity.this, menuBtn);

                popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        ImageView iv = findViewById(R.id.sushiImg);
                        if (item.getTitle().equals("ORIGINAL")) {
                            img.setPixels(mon_image, 0, w, 0, 0, w, h);
                            dessinHisto(img, hR, hG, hB);
                        }
                        if (item.getTitle().equals("GREY ME")) {
                            toGrey(img);
                            dessinHisto(img, hR, hG, hB);
                        }
                        if (item.getTitle().equals("RAND ME")) {
                            colorize(img);
                            dessinHisto(img, hR, hG, hB);
                        }
                        if (item.getTitle().equals("JUST RED")) {
                            just_red(img);
                            dessinHisto(img, hR, hG, hB);
                        }
                        if (item.getTitle().equals("CONTRASTE")) {
                            contraste(img);
                            dessinHisto(img, hR, hG, hB);
                        }
                        iv.setImageBitmap(img);
                        return true;
                    }
                });
                popup.show();
            }
        });
    }

    void toGrey(Bitmap bmp) {
        int w = bmp.getWidth();
        int h = bmp.getHeight();

        int[] nb_pix = new int[w * h];
        int i = 0;

        bmp.getPixels(nb_pix, 0, w, 0, 0, w, h);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int r = Color.red(nb_pix[i]);
                int g = Color.green(nb_pix[i]);
                int b = Color.blue(nb_pix[i]);

                int moy = (int) (0.3 * r + 0.59 * g + 0.11 * b);

                nb_pix[i] = Color.rgb(moy, moy, moy);
                i++;
            }
        }
        bmp.setPixels(nb_pix, 0, w, 0, 0, w, h);
    }

    int[] tabHisto(int[] nb_pix, float[] histo, int w, int h, String color){
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < (h * histo[x]); y++) {
                int i = ((h * w) - w) - (y * w) + x;
                switch (color) {
                    case "red":
                        nb_pix[i] = Color.RED;
                        break;
                    case "green":
                        nb_pix[i] = Color.GREEN;
                        break;
                    case "blue":
                        nb_pix[i] = Color.BLUE;
                        break;
                }
            }
        }
        return nb_pix;
    }

    float normalise(float[] histo){
        float max = 0;
        for(int i = 0; i < histo.length; i++){
            if(histo[i] > max){
                max = histo[i];
            }
        }
        return max;
    }

    void dessinHisto(Bitmap bmp, Bitmap hR, Bitmap hG, Bitmap hB){
        int w = hR.getWidth();
        int h = hR.getHeight();

        int[] nb_hR = new int[hR.getWidth() * hR.getHeight()];
        int[] nb_hG = new int[hG.getWidth() * hG.getHeight()];
        int[] nb_hB = new int[hB.getWidth() * hB.getHeight()];

        float[] histoR = valueHisto(bmp, "red");
        float[] histoG = valueHisto(bmp, "green");
        float[] histoB = valueHisto(bmp, "blue");

        hR.getPixels(nb_hR, 0, hR.getWidth(), 0, 0, hR.getWidth(), hR.getHeight());
        hG.getPixels(nb_hG, 0, hG.getWidth(), 0, 0, hG.getWidth(), hG.getHeight());
        hB.getPixels(nb_hB, 0, hB.getWidth(), 0, 0, hB.getWidth(), hB.getHeight());

        float[] moyR = new float[256];
        float[] moyG = new float[256];
        float[] moyB = new float[256];
        float maxR = normalise(histoR);
        float maxG = normalise(histoG);
        float maxB = normalise(histoB);

        for(int i = 0; i < moyR.length; i++){
            moyR[i] = histoR[i] / maxR;
            moyG[i] = histoG[i] / maxG;
            moyB[i] = histoB[i] / maxB;
        }

        nb_hR = tabHisto(nb_hR, moyR, w, h, "red");
        nb_hG = tabHisto(nb_hG, moyG, w, h, "green");
        nb_hB = tabHisto(nb_hB, moyB, w, h, "blue");

        hR.setPixels(nb_hR, 0, w, 0, 0, w, h);
        hG.setPixels(nb_hG, 0, hG.getWidth(), 0, 0, hG.getWidth(), hG.getHeight());
        hB.setPixels(nb_hB, 0, hB.getWidth(), 0, 0, hB.getWidth(), hB.getHeight());
    }

    float[] valueHisto(Bitmap bmp, String color) {    // 0 : red | 1 : blue | 2 : green
        int w = bmp.getWidth();
        int h = bmp.getHeight();

        float[] histo = new float[256];

        for (int i = 0; i < histo.length; i++) {
            histo[i] = 0;
        }

        int[] nb_pix = new int[w * h];

        bmp.getPixels(nb_pix, 0, w, 0, 0, w, h);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int i = y * w + x;
                int value = 0;
                switch (color) {
                    case "red":
                        value = Color.red(nb_pix[i]);
                        break;
                    case "blue":
                        value = Color.blue(nb_pix[i]);
                        break;
                    case "green":
                        value = Color.green(nb_pix[i]);
                        break;
                }
                histo[value]++;
            }
        }
        return histo;
    }

    int max(Bitmap bmp, String color) {
        float[] histo = valueHisto(bmp, color);
        for (int i = 255; i >= 0; i--) {
            if (histo[i] > 10) {
                return i;
            }
        }
        return 0;
    }

    int min(Bitmap bmp, String color) {
        float[] histo = valueHisto(bmp, color);
        for (int i = 0; i < histo.length; i++) {
            if (histo[i] > 10) {
                return i;
            }
        }
        return 0;
    }

    void contraste(Bitmap bmp) {
        float[] LUTR = new float[256];
        float[] LUTG = new float[256];
        float[] LUTB = new float[256];

        int w = bmp.getWidth();
        int h = bmp.getHeight();

        int[] nb_pix = new int[w * h];

        bmp.getPixels(nb_pix, 0, w, 0, 0, w, h);

        int maxR = max(bmp, "red");  // inverser min et max donne un filtre négatif
        int minR = min(bmp, "red");


        int maxG = max(bmp, "green");
        int minG = min(bmp, "green");

        int maxB = max(bmp, "blue");
        int minB = min(bmp, "blue");

        for (int ng = 0; ng < 256; ng++) {
            LUTR[ng] = (255 * (ng - minR)) / (maxR - minR);
            LUTB[ng] = (255 * (ng - minB)) / (maxB - minB);
            LUTG[ng] = (255 * (ng - minG)) / (maxG - minG);
        }

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int i = y * w + x;
                int r = (int) LUTR[Color.red(nb_pix[i])];
                int g = (int) LUTG[Color.green(nb_pix[i])];
                int b = (int) LUTB[Color.blue(nb_pix[i])];

                nb_pix[i] = Color.rgb(r, g, b);
            }
        }
        bmp.setPixels(nb_pix, 0, w, 0, 0, w, h);
    }

    void just_red(Bitmap bmp) {
        int w = bmp.getWidth();
        int h = bmp.getHeight();

        int[] nb_pix = new int[w * h];
        int i = 0;

        bmp.getPixels(nb_pix, 0, w, 0, 0, w, h);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int r = Color.red(nb_pix[i]);
                int g = Color.green(nb_pix[i]);
                int b = Color.blue(nb_pix[i]);


                float[] hsv = new float[3];
                new_RGBToHSV(r, g, b, hsv);

                float hue = hsv[0]; // teinte
                float sat = hsv[1]; // saturation
                float val = hsv[2];

                if (hue <= 10 || hue >= 355) {
                    nb_pix[i] = new_HSVToRGB(r, g, b, new float[]{hue, sat, val});
                } else {
                    int moy = (int) (0.3 * r + 0.59 * g + 0.11 * b);

                    nb_pix[i] = Color.rgb(moy, moy, moy);
                }
                i++;
            }
        }
        bmp.setPixels(nb_pix, 0, w, 0, 0, w, h);
    }

    void colorize(Bitmap bmp) {
        int w = bmp.getWidth();
        int h = bmp.getHeight();

        int[] nb_pix = new int[w * h];
        int i = 0;

        Random c = new Random();
        float rc = c.nextFloat() * 360; // trouve un nb random

        bmp.getPixels(nb_pix, 0, w, 0, 0, w, h);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {

                int r = Color.red(nb_pix[i]);
                int g = Color.green(nb_pix[i]);
                int b = Color.blue(nb_pix[i]);


                float[] hsv = new float[3];
                new_RGBToHSV(r, g, b, hsv);

                hsv[0] = rc;
                float hue = hsv[0]; // teinte
                float sat = hsv[1]; // saturation
                float val = hsv[2];

                nb_pix[i] = new_HSVToRGB(r, g, b, new float[]{hue, sat, val});
                i++;
            }
        }
        bmp.setPixels(nb_pix, 0, w, 0, 0, w, h);
    }

    void new_RGBToHSV(int r, int g, int b, float[] hsv) {
        float r2 = r / 255.f;
        float g2 = g / 255.f;
        float b2 = b / 255.f;

        float rgbmax = Math.max(r2, g2);
        float cmax = Math.max(rgbmax, b2);

        float rgbmin = Math.min(r2, g2);
        float cmin = Math.min(rgbmin, b2);

        float d = cmax - cmin;

        float h = 0;

        if (d == 0) {
            h = 0;
        } else if (cmax == r2) {
            h = 60 * (((g2 - b2) % 6) / d);
        } else if (cmax == g2) {
            h = 60 * (((b2 - r2) / d) + 2);
        } else if (cmax == b2) {
            h = 60 * (((r2 - g2) / d) + 4);
        }

        float s = 0;

        if (cmax == 0) {
            s = 0;
        } else {
            s = d / cmax;
        }

        hsv[0] = h;
        hsv[1] = s;
        hsv[2] = cmax;
    }

    int new_HSVToRGB(int r, int g, int b, float[] hsv) {
        float c = hsv[2] * hsv[1];

        float x = c * (1 - Math.abs((hsv[0] / 60) % 2 - 1));

        float m = hsv[2] - c;

        float r2 = 0;
        float g2 = 0;
        float b2 = 0;

        if (hsv[0] >= 0f && hsv[0] < 60f) {
            r2 = c;
            g2 = x;
            b2 = 0;
        } else if (hsv[0] >= 60f && hsv[0] < 120f) {
            r2 = x;
            g2 = c;
            b2 = 0;
        } else if (hsv[0] >= 120f && hsv[0] < 180f) {
            r2 = 0;
            g2 = c;
            b2 = x;
        } else if (hsv[0] >= 180f && hsv[0] < 240f) {
            r2 = 0;
            g2 = x;
            b2 = c;
        } else if (hsv[0] >= 240f && hsv[0] < 300f) {
            r2 = x;
            g2 = 0;
            b2 = c;
        } else if (hsv[0] >= 300f && hsv[0] < 360f) {
            r2 = c;
            g2 = 0;
            b2 = x;
        }
        return Color.rgb((int) ((r2 + m) * 255), (int) ((g2 + m) * 255), (int) ((b2 + m) * 255));
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }
}
