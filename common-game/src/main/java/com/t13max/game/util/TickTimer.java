package com.t13max.game.util;

import com.t13max.util.TimeUtil;
import lombok.Getter;


/**
 * @author t13max
 * @since 16:45 2024/12/9
 */
@Getter
public class TickTimer {

    private boolean running = false;
    private long interval = -1L;
    private long nextTime = -1L;
    private boolean intervalCheckLog;

    public TickTimer() {
    }

    public TickTimer(long interval) {
        this.start(interval);
    }

    public TickTimer(long timeStart, long interval) {
        this.start(timeStart, interval);
    }

    public TickTimer(long interval, boolean immediate) {
        this.start(interval, immediate);
    }

    public TickTimer(long timeStart, long interval, boolean immediate) {
        this.start(timeStart, interval, immediate);
    }

    public void start(long interval) {
        this.start(interval, false);
    }

    public void start(long timeStart, long interval) {
        this.start(timeStart, interval, false);
    }

    public void start(long interval, boolean immediate) {
        long now = TimeUtil.nowMills();
        this.start(now, interval, immediate);
    }

    public void start(long timeStart, long interval, boolean immediate) {
        this.interval = interval;
        if (immediate) {
            this.nextTime = timeStart;
        } else {
            this.nextTime = timeStart + interval;
        }

        this.running = true;
    }

    public void stop() {
        this.running = false;
    }

    public boolean isOnce(long now) {
        if (!this.running) {
            return false;
        } else if (this.nextTime > now) {
            return false;
        } else {
            this.stop();
            return true;
        }
    }

    public boolean isPeriod(long now) {
        return this.isPeriod(now, true);
    }

    public boolean isPeriod(long now, boolean needRefix) {
        if (!this.running) {
            return false;
        } else {
            if (this.interval <= 0L && !this.intervalCheckLog) {
                this.intervalCheckLog = true;
            }

            if (this.nextTime > now) {
                return false;
            } else {
                if (needRefix) {
                    this.nextTime = now + this.interval;
                } else {
                    this.nextTime += this.interval;
                }

                return true;
            }
        }
    }

    public boolean isStarted() {
        return this.running;
    }

    public long getTimeLeft(long curr) {
        if (!this.running) {
            return 0L;
        } else {
            return this.nextTime <= curr ? 0L : this.nextTime - curr;
        }
    }

    public void reStart() {
        this.nextTime = TimeUtil.nowMills() + this.interval;
        this.running = true;
    }

    public void setTimeNext(long timeNext) {
        this.nextTime = timeNext;
    }

    public void extendTimeNext(int extend) {
        this.nextTime +=  extend;
    }

    /*public void writeTo(OutputStream out) throws IOException {
        out.write(this.running);
        out.write(this.interval);
        out.write(this.nextTime);
    }*/

    /*public void readFrom(InputStream in) throws IOException {
        this.running = (Boolean) in.read();
        this.interval = (Long) in.read();
        this.nextTime = (Long) in.read();
    }*/
}
