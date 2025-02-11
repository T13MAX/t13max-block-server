package com.t13max.game.entity.module.skill.overlay;


import com.t13max.game.entity.IEntity;
import com.t13max.game.entity.PlayerEntity;
import com.t13max.game.util.TickTimer;
import com.t13max.template.helper.MagicHelper;
import com.t13max.template.manager.TemplateManager;
import com.t13max.template.temp.TemplateMagic;

import java.util.concurrent.TimeUnit;

/**
 * @Author t13max
 * @Date 16:45 2024/12/12
 */
public class MagicOverlayData {
    private final IEntity owner;
    private final int templateId;
    private final TickTimer objOverlayTimer = new TickTimer();
    private int currOverlay;
    private int maxOverlay;
    private long lastRecoverTime;

    private long lastCheckTime;

    public MagicOverlayData(IEntity owner, TemplateMagic templateMagic) {
        this.owner = owner;
        this.templateId = templateMagic.id;
        this.maxOverlay = templateMagic.overlay;
        this.currOverlay = this.maxOverlay;
        this.lastRecoverTime = 0L;
    }

    public void pulse(long now) {
        updateOverlay(now);

        if (this.objOverlayTimer.isPeriod(now)) {
            this.currOverlay++;
            this.lastRecoverTime = now;

            if (this.currOverlay >= this.maxOverlay) {
                this.stopTimer();
            }

            this.sendMagicOverlayUpdate();
        }
    }

    public boolean hasOverlay() {
        return this.currOverlay > 0;
    }

    public boolean isStarted() {
        return this.objOverlayTimer.isStarted();
    }

    public void onMagicCastEnded() {
        this.currOverlay = Math.max(0, this.currOverlay - 1);

        if (this.currOverlay < this.maxOverlay && !this.isStarted()) {
            this.startTimer();
        }

        this.sendMagicOverlayUpdate();
    }

    public void updateOverlay(long now) {
        if (now < lastCheckTime + TimeUnit.SECONDS.toMillis(1)) {
            return;
        }
        TemplateMagic templateMagic = TemplateManager.inst().helper(MagicHelper.class).getTemplate(templateId);
        int oMaxOverlay = this.maxOverlay;

        this.maxOverlay = templateMagic.overlay;

        if (oMaxOverlay < this.maxOverlay) {
            if (!objOverlayTimer.isStarted()) {
                this.startTimer();
            }

            this.sendMagicOverlayUpdate();
        }
        lastCheckTime = now;
    }

    private void startTimer() {
        TemplateMagic templateMagic = TemplateManager.inst().helper(MagicHelper.class).getTemplate(templateId);
        this.objOverlayTimer.start(templateMagic.overlayRecovery);
        this.lastRecoverTime = owner.getTime();
    }

    private void stopTimer() {
        this.objOverlayTimer.stop();
        this.lastRecoverTime = 0L;
    }

    public void sendMagicOverlayUpdate() {
        if (!owner.isKindOf(PlayerEntity.class)) {
            return;
        }

        //owner.sendMsg();
    }
}
