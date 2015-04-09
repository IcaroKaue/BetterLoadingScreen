package alexiil.mods.load;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import org.lwjgl.opengl.GL11;

import alexiil.mods.load.ProgressDisplayer.IDisplayer;

public class MinecraftDisplayer implements IDisplayer {
    private ResourceLocation locationMojangPng = new ResourceLocation("textures/gui/title/mojang.png");
    private ResourceLocation locationProgressBar = new ResourceLocation("betterloadingscreen/textures/progressBars.png");
    private TextureManager textureManager = null;
    private FontRenderer fontRenderer = null;
    private ScaledResolution resolution = null;
    private Framebuffer framebuffer = null;
    private Minecraft mc = null;
    private boolean callAgain = false;
    private double startTexLocation = 74;

    // Minecraft's display hasn't been created yet, so don't bother trying
    // to do anything now
    @Override
    public void open(Configuration cfg) {
        String comment =
                "The type of progress bar to display. Use either 0, 1 or 2. (0 is the experiance bar, 1 is the boss health bar, and 2 is the horse jump bar)";
        Property prop = cfg.get("general", "progressType", 1, comment, 0, 2);
        startTexLocation = prop.getInt() * 10;

        comment =
                "The location of the progress bar. You can chnage this to a different one, or a different resource pack. Note that this WILL crash minecraft, or not work if this is set incorrectly";
        prop = cfg.get("general", "progressBarLocation", "betterloadingscreen:textures/progressBars.png", comment);
        locationProgressBar = new ResourceLocation(prop.getString());
    }

    @Override
    public void displayProgress(String text, float percent) {
        // if (Minecraft.getMinecraft().renderEngine == null)
        // return;
        mc = Minecraft.getMinecraft();
        resolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);

        preDisplayScreen();
        float sf = resolution.getScaleFactor();
        GL11.glScalef(sf, sf, sf);

        int centerX = resolution.getScaledWidth() / 2;
        int centerY = resolution.getScaledHeight() / 2;

        drawCenteredString(text, centerX, centerY + 30);
        drawCenteredString((int) (percent * 100) + "%", centerX, centerY + 40);

        GL11.glColor4f(1, 1, 1, 1);

        textureManager.bindTexture(locationProgressBar);

        double texWidth = 182;
        double startX = centerX - texWidth / 2;
        drawTexturedModalRect(startX, centerY + 50, 0, startTexLocation, texWidth, 5);
        drawTexturedModalRect(startX, centerY + 50, 0, startTexLocation + 5, percent * texWidth, 5);

        sf = 1 / sf;
        GL11.glScalef(sf, sf, sf);
        postDisplayScreen();
        if (callAgain) {
            // For some reason, calling this again makes pre-init render properly. I have no idea why, it just does
            callAgain = false;
            displayProgress(text, percent);
        }
    }

    // Taken from net.minecraft.client.gui.Gui
    public void drawTexturedModalRect(double x, double y, double u, double z, double width, double height) {
        float f = 0.00390625F;
        float f1 = 0.00390625F;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + height, 0, u * f, (z + height) * f1);
        tessellator.addVertexWithUV(x + width, y + height, 0, (u + width) * f, (z + height) * f1);
        tessellator.addVertexWithUV(x + width, y, 0, (u + width) * f, z * f1);
        tessellator.addVertexWithUV(x, y, 0, u * f, z * f1);
        tessellator.draw();
    }

    private void drawCenteredString(String string, int xCenter, int yPos) {
        int width = fontRenderer.getStringWidth(string);
        fontRenderer.drawString(string, xCenter - width / 2, yPos, 0);
    }

    private void preDisplayScreen() {
        if (textureManager == null) {
            textureManager = mc.renderEngine = new TextureManager(mc.getResourceManager());

            mc.fontRenderer = new FontRenderer(mc.gameSettings, new ResourceLocation("textures/font/ascii.png"), textureManager, false);
            if (mc.gameSettings.language != null) {
                mc.fontRenderer.setUnicodeFlag(mc.func_152349_b());
                LanguageManager lm = mc.getLanguageManager();
                mc.fontRenderer.setBidiFlag(lm.isCurrentLanguageBidirectional());
            }
            mc.fontRenderer.onResourceManagerReload(mc.getResourceManager());
            callAgain = true;
        }
        if (fontRenderer != mc.fontRenderer)
            fontRenderer = mc.fontRenderer;
        if (textureManager != mc.renderEngine)
            textureManager = mc.renderEngine;
        resolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int i = resolution.getScaleFactor();
        framebuffer = new Framebuffer(resolution.getScaledWidth() * i, resolution.getScaledHeight() * i, true);
        framebuffer.bindFramebuffer(false);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0.0D, (double) resolution.getScaledWidth(), (double) resolution.getScaledHeight(), 0.0D, 1000.0D, 3000.0D);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        GL11.glTranslatef(0.0F, 0.0F, -2000.0F);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_FOG);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        // This also means that you can override the mojang image :P
        textureManager.bindTexture(locationMojangPng);

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setColorOpaque_I(16777215);
        tessellator.addVertexWithUV(0.0D, (double) mc.displayHeight, 0.0D, 0.0D, 0.0D);
        tessellator.addVertexWithUV((double) mc.displayWidth, (double) mc.displayHeight, 0.0D, 0.0D, 0.0D);
        tessellator.addVertexWithUV((double) mc.displayWidth, 0.0D, 0.0D, 0.0D, 0.0D);
        tessellator.addVertexWithUV(0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
        tessellator.draw();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        tessellator.setColorOpaque_I(16777215);
        short short1 = 256;
        short short2 = 256;
        mc.scaledTessellator((resolution.getScaledWidth() - short1) / 2, (resolution.getScaledHeight() - short2) / 2, 0, 0, short1, short2);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_FOG);

        framebuffer.unbindFramebuffer();
        framebuffer.framebufferRender(resolution.getScaledWidth() * i, resolution.getScaledHeight() * i);
    }

    private void postDisplayScreen() {
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
        GL11.glFlush();
        mc.func_147120_f();
    }

    @Override
    public void close() {}
}