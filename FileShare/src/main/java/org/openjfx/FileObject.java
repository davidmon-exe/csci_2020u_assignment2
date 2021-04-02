package org.openjfx;

import java.io.File;

public class FileObject {
    private File file;
    private String fileName;
    public  FileObject(File file, String name){
        this.file = file;
        this.fileName = name;
    }
}
