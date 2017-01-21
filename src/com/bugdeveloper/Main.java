package com.bugdeveloper;

import java.io.File;
import java.io.IOException;

/**
 * Created by bugdeveloper on 21.01.17.
 */
public class Main {

    public static void main(String[] args) throws IOException {

        PostGenerator pg = new PostGenerator("quotes.txt", "pictures.txt", 3, 30, 20, 50);
        String[] post = pg.generatePost();
        for (int i = 0; i < post.length; i++)
            System.out.println(post[i]);
    }
}
