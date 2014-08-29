package com.pomeloapp.pomelo.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.File;
import java.io.IOException;

/**
 * Created by sudhagar-1838 on 8/15/14.
 */
public class FileUtil
{

    public static long folderSize(File directory)
    {
        long size = 0;
        for (File file : directory.listFiles())
        {
            if (file.isFile())
            {
                size += file.length();
            }
            else
            {
                size += folderSize(file);
            }
        }
        return size;
    }

    public static void delete(File f) throws IOException
    {
        if (f.isDirectory())
        {
            for (File c : f.listFiles())
                delete(c);
        }
        if (!f.delete())
        {
            throw new IOException("Failed to delete file: " + f);
        }
    }

    public static void delete(String path) throws IOException
    {
        delete(new File(path));
    }

    public static void openFile(Context context, File url) throws IOException
    {
        File file = url;
        Uri uri = Uri.fromFile(file);

        Intent intent = new Intent(Intent.ACTION_VIEW);

        if (url.toString().contains(".doc") || url.toString().contains(".docx"))
        {
            // Word document
            intent.setDataAndType(uri, "application/msword");
        }
        else if(url.toString().contains(".pdf"))
        {
            // PDF file
            intent.setDataAndType(uri, "application/pdf");
        }
        else if(url.toString().contains(".ppt") || url.toString().contains(".pptx"))
        {
            // Powerpoint file
            intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        }
        else if(url.toString().contains(".xls") || url.toString().contains(".xlsx"))
        {
            // Excel file
            intent.setDataAndType(uri, "application/vnd.ms-excel");
        }
        else if(url.toString().contains(".zip") || url.toString().contains(".rar"))
        {
            // ZIP Files
            intent.setDataAndType(uri, "application/zip");
        }
        else if(url.toString().contains(".rtf"))
        {
            // RTF file
            intent.setDataAndType(uri, "application/rtf");
        }
        else if(url.toString().contains(".wav") || url.toString().contains(".mp3"))
        {
            // WAV audio file
            intent.setDataAndType(uri, "audio/x-wav");
        }
        else if(url.toString().contains(".gif"))
        {
            // GIF file
            intent.setDataAndType(uri, "image/gif");
        }
        else if(url.toString().contains(".jpg") || url.toString().contains(".jpeg") || url.toString().contains(".png"))
        {
            // JPG file
            intent.setDataAndType(uri, "image/jpeg");
        }
        else if(url.toString().contains(".txt"))
        {
            // Text file
            intent.setDataAndType(uri, "text/plain");
        }
        else if(url.toString().contains(".3gp") || url.toString().contains(".mpg") || url.toString().contains(".mpeg") || url.toString().contains(".mpe") || url.toString().contains(".mp4") || url.toString().contains(".avi")) {
            // Video files
            intent.setDataAndType(uri, "video/*");
        }
        else
        {
            intent.setDataAndType(uri, "*/*");
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
