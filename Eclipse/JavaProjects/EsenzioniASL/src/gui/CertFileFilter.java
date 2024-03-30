package gui;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class CertFileFilter extends FileFilter {

    //Accept all directories and all certificate format files.
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String extension = getExtension(f);
        if (extension != null) {
            if (extension.equals(Utils.cer) ||
                extension.equals(Utils.pem) ||
                extension.equals(Utils.der)) {
                    return true;
            } else {
                return false;
            }
        }

        return false;
    }

    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }

    //The description of this filter
    public String getDescription() {
        return "Solo certificati";
    }

    class Utils {

	    public final static String cer = "cer";
	    public final static String pem = "pem";
	    public final static String der = "der";
	}
}
