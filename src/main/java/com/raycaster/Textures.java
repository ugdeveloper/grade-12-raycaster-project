package com.raycaster;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class Textures {

    private static List<File> textureFiles = new ArrayList<>();
    static int[] T_1, T_2, T_3, T_4, T_5;

    static {
        for (int i = 1; i < 6; i++) {
            textureFiles.add(new File("textures/T_" + i + ".txt"));
        }

        for (int i = 0; i < textureFiles.size(); i++) {
            try (BufferedReader br = new BufferedReader(
                    new FileReader(textureFiles.get(i)))) {

                List<Integer> data = new ArrayList<>();

                String line;
                while ((line = br.readLine()) != null) {
                    data.add(Integer.parseInt(line));
                }

                int[] texture = new int[data.size()];
                for (int n = 0; n < data.size(); n++) {
                    texture[n] = data.get(n);
                }

                if (i == 0) {
                    T_1 = texture;
                } else if (i == 1) {
                    T_2 = texture;
                } else if (i == 2){
                    T_3 = texture;
                } else if(i == 3) {
                    T_4 = texture;
                } else {
                    T_5 = texture;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
