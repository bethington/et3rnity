package com.riiablo.logger;

import java.io.OutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class RiiabloEncoder extends SimpleEncoder {
  private static final int MAX_DEPTH = 256;
  private static final int DEPTH_STEP = 2;
  private final CharSequence spaces = StringUtils.repeat(' ', MAX_DEPTH * DEPTH_STEP);

  private final StringBuilder buffer = new StringBuilder(1024);

  private Thread context;
  private boolean fullMode;
  private StringMap mdc;
  private int depth;

  public boolean isFullMode() {
    return fullMode;
  }

  public void setFullMode(boolean b) {
    this.fullMode = b;
  }

  @Override
  public void encode(LogEvent event, OutputStream out) {
    try {
      if (fullMode) {
        encodeFullMode(event, buffer);
      } else {
        encodeCompactMode(event, buffer);
      }
      out.write(buffer.toString().getBytes(US_ASCII));
      newLine(out);
      encodeStackTrace(event, out);
    } catch (Throwable t) {
      ExceptionUtils.rethrow(t);
    } finally {
      buffer.setLength(0);
    }
  }

  private void encodeFullMode(LogEvent event, StringBuilder buffer) {
    final StringMap mdc = event.mdc();
    encodeMessage(event, buffer);
    encodeFullMDC(mdc, buffer);
  }

  private void encodeFullMDC(StringMap mdc, StringBuilder buffer) {
    if (mdc.isEmpty()) return;
    buffer.append(' ');
    buffer.append(mdc.toString());
  }

  private void encodeCompactMode(LogEvent event, StringBuilder buffer) {
    final StringMap mdc = event.mdc();
    final int depth = mdc.size();
    if (depth > 0) {
      int startingDepth = this.depth;
      final Thread currentThread = Thread.currentThread();
      if (context != currentThread) {
        context = currentThread;
        startingDepth = 0;
      }

      if (!mdc.equals(this.mdc)) {
        final int dirty = mdc.getDirtyDepth();
        if (dirty < depth && dirty < startingDepth) {
          startingDepth = dirty;
        }

        if (this.mdc != null) {
          final int diff = this.mdc.firstDifference(mdc, startingDepth);
          if (diff >= depth) {
            startingDepth = Math.max(depth - 1, 0);
          } else {
            startingDepth = diff;
          }
        }

        encodeCompactMDC(mdc, buffer, startingDepth, depth);
        assert this.depth == depth;
        this.mdc = mdc;
      }

      buffer.append(spaces, 0, depth * DEPTH_STEP);
    }

    encodeMessage(event, buffer);
  }

  private void encodeCompactMDC(StringMap mdc, StringBuilder buffer, int startingDepth, int depth) {
    for (int d = startingDepth; d < depth; d++) {
      buffer.append(spaces, 0, d * DEPTH_STEP);
      buffer.append(' ');
      buffer.append('{');
      mdc.appendEntry(d, buffer);
      buffer.append('}');
      buffer.append(lineSeparator);
    }

    this.depth = depth;
  }
}
