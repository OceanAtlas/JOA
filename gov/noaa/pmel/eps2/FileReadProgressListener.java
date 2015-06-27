package gov.noaa.pmel.eps2;

import java.awt.*;
import java.awt.event.*;

public interface FileReadProgressListener {
    public void percentChange(FileReadProgressEvent evt);
}