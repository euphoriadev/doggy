/*
 * Created on Feb 10, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.cmc.music.util;

/**
 * @author charles
 * <p>
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class FileComparator {
    public boolean compare(File a, File b) {
        System.out.println("comparing: " + a.getAbsolutePath());
        System.out.println("\t" + "with: " + b.getAbsolutePath());

        // identity.
        if (a.getAbsolutePath().equals(b.getAbsolutePath()))
            return false;

        if (a.length() != b.length()) {
            System.out.println("\t" + "lengths don't match");
            return false;
        }

        boolean result = compareContents(a, b);
        if (result)
            System.out.println("\t" + "match!");
        else
            System.out.println("\t" + "contents don't match");
        return result;

    }

    private boolean compareContents(File a, File b) {
        InputStream ais = null;
        InputStream bis = null;

        try {
            ais = new FileInputStream(a);
            ais = new BufferedInputStream(ais);
            bis = new FileInputStream(b);
            bis = new BufferedInputStream(bis);

            while (true) {
                int abyte = ais.read();
                int bbyte = bis.read();
                if (abyte != bbyte)
                    return false;
                if (abyte == -1)
                    return true;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (ais != null)
                    ais.close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            try {
                if (bis != null)
                    bis.close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }

}