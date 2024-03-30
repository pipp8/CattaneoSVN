package gui;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class TextFileFilter extends FileFilter {

    //Accept all directories and all gif, jpg, tiff, or png files.
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String extension = getExtension(f);
        if (extension != null) {
            if (extension.equals(Utils.txt) ||
                extension.equals(Utils.csv)) {
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
        return "Solo file di testo";
    }

    class Utils {

	    public final static String txt = "txt";
	    public final static String csv = "csv";
	}
}
