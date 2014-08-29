package com.pomeloapp.pomelo.util;

import java.io.File;

/**
 * Created by sudhagar-1838 on 8/15/14.
 */
public class CommonUtil
{

    static public File[] arrayConcat(Object[] A, Object[] B)
    {
        int aLen = A.length;
        int bLen = B.length;
        File[] C= new File[aLen+bLen];
        System.arraycopy(A, 0, C, 0, aLen);
        System.arraycopy(B, 0, C, aLen, bLen);
        return C;
    }

    static public String convertBytes(long bytes)
    {
        int totalKBytes = (int) (bytes / 1024 );
        if(totalKBytes<1024)
        {
            return totalKBytes + " Kb";
        }

        int totalMbytes = (int) (totalKBytes / 1024) ;
        if(totalMbytes<400)
        {
            return totalMbytes + " Mb";
        }
        else
        {
            float totalGbytes = (float)totalMbytes / 1024;
            return String.format("%.1f", totalGbytes) + " Gb";
        }
    }
}
