package com.scpmod.scpgate.renderer;

import com.scpmod.scpgate.SCPGateMod;
import com.scpmod.scpgate.blocks.BlockSCPGate;
import com.scpmod.scpgate.tileentity.TileEntitySCPGate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * Renderizza il cancello SCP come due pannelli scorrevoli (4 blocchi larghi, 3 alti, 0.5 profondi).
 *
 * Sistema di coordinate locale (orientamento NORTH):
 *   X: 0 → 4  (larghezza, pannello sinistro 0-2, destro 2-4)
 *   Y: 0 → 3  (altezza)
 *   Z: 0.25 → 0.75  (profondità centrata nel blocco)
 *
 * Apertura: il pannello sinistro scivola verso -X, quello destro verso +X.
 */
public class TESRSCPGate extends TileEntitySpecialRenderer<TileEntitySCPGate> {

    private static final ResourceLocation TEX_PANEL =
            new ResourceLocation(SCPGateMod.MODID, "textures/blocks/scp_gate_panel.png");
    private static final ResourceLocation TEX_SIDE  =
            new ResourceLocation(SCPGateMod.MODID, "textures/blocks/scp_gate_side.png");

    @Override
    public void render(TileEntitySCPGate te, double x, double y, double z,
                       float partialTicks, int destroyStage, float alpha) {

        if (te == null || te.getWorld() == null) return;

        IBlockState state = te.getWorld().getBlockState(te.getPos());
        if (!(state.getBlock() instanceof BlockSCPGate)) return;

        EnumFacing facing = state.getValue(BlockSCPGate.FACING);
        float open = te.getOpenProgress(partialTicks); // 0 = chiuso, 1 = aperto

        // Illuminazione dal blocco
        int light = te.getWorld().getCombinedLight(te.getPos(), 0);
        float blockLight = (light & 0xFFFF) / 15.0f;
        float skyLight   = (light >> 16 & 0xFFFF) / 15.0f;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        // Rotazione in base alla direzione della porta
        applyFacingRotation(facing);

        // Imposta illuminazione
        OpenGlHelper.setLightmapTextureCoords(
            OpenGlHelper.lightmapTexUnit,
            Math.max(blockLight, open > 0 ? 0.5f : 0.1f) * 240,
            skyLight * 240
        );

        GlStateManager.enableBlend();
        GlStateManager.disableCull();

        // Pannello SINISTRO (X: 0→2) — scivola a sinistra
        float leftOffset = -open * 2.0f;
        renderPanel(leftOffset, 0, 0, 2, 3, 0.25f, 0.75f, true);

        // Pannello DESTRO (X: 2→4) — scivola a destra
        float rightOffset = open * 2.0f;
        renderPanel(2 + rightOffset, 0, 0, 2, 3, 0.25f, 0.75f, false);

        GlStateManager.enableCull();
        GlStateManager.disableBlend();

        GlStateManager.popMatrix();

        // Ripristina illuminazione standard
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
    }

    // ─── Rotazione per orientamento ───────────────────────────────────────────

    private void applyFacingRotation(EnumFacing facing) {
        /*
         * Coordinata locale di default: porta nello spazio XY, profondità in Z.
         * NORTH: nessuna rotazione (front face verso -Z)
         * SOUTH: ruota 180° → la struttura occupa lo stesso spazio XZ ma
         *        la faccia front diventa quella verso +Z
         * EAST:  ruota +90° intorno Y, poi traslazione per restare nel cubo
         * WEST:  ruota -90° intorno Y, poi traslazione
         */
        switch (facing) {
            case NORTH:
                // default
                break;
            case SOUTH:
                GlStateManager.translate(4, 0, 1);
                GlStateManager.rotate(180, 0, 1, 0);
                break;
            case EAST:
                GlStateManager.translate(1, 0, 0);
                GlStateManager.rotate(90, 0, 1, 0);
                break;
            case WEST:
                GlStateManager.translate(0, 0, 4);
                GlStateManager.rotate(-90, 0, 1, 0);
                break;
        }
    }

    // ─── Disegno singolo pannello ─────────────────────────────────────────────

    /**
     * @param startX  posizione X locale del bordo sinistro del pannello
     * @param startY  posizione Y locale inferiore
     * @param panelX  offset X dentro la texture (0 = metà sinistra, 0.5 = metà destra)
     * @param w       larghezza in blocchi (sempre 2)
     * @param h       altezza in blocchi (sempre 3)
     * @param z1/z2   profondità (0.25 → 0.75)
     * @param leftPanel true = usa la metà sinistra della texture, false = destra
     */
    private void renderPanel(float startX, float startY,
                             float panelX, float w, float h,
                             float z1, float z2,
                             boolean leftPanel) {
        float endX = startX + w;
        float endY = startY + h;

        // UV: la texture (128×192) è divisa a metà per i due pannelli
        float uMin = leftPanel ? 0.0f : 0.5f;
        float uMax = leftPanel ? 0.5f : 1.0f;
        float vMin = 0.0f, vMax = 1.0f;

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        // ── Faccia FRONTALE (Z = z1, normale verso -Z) ──────────────────────
        Minecraft.getMinecraft().getTextureManager().bindTexture(TEX_PANEL);
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(startX, endY, z1).tex(uMin, vMin).endVertex();
        buf.pos(endX,   endY, z1).tex(uMax, vMin).endVertex();
        buf.pos(endX,  startY, z1).tex(uMax, vMax).endVertex();
        buf.pos(startX,startY, z1).tex(uMin, vMax).endVertex();
        tess.draw();

        // ── Faccia POSTERIORE (Z = z2, normale verso +Z) ────────────────────
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(endX,   endY, z2).tex(uMin, vMin).endVertex();
        buf.pos(startX, endY, z2).tex(uMax, vMin).endVertex();
        buf.pos(startX,startY, z2).tex(uMax, vMax).endVertex();
        buf.pos(endX,  startY, z2).tex(uMin, vMax).endVertex();
        tess.draw();

        // ── Lati, top, bottom: texture semplice laterale ────────────────────
        Minecraft.getMinecraft().getTextureManager().bindTexture(TEX_SIDE);

        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        // Top
        buf.pos(startX, endY, z2).tex(0, 0).endVertex();
        buf.pos(endX,   endY, z2).tex(1, 0).endVertex();
        buf.pos(endX,   endY, z1).tex(1, 1).endVertex();
        buf.pos(startX, endY, z1).tex(0, 1).endVertex();

        // Bottom
        buf.pos(startX, startY, z1).tex(0, 0).endVertex();
        buf.pos(endX,   startY, z1).tex(1, 0).endVertex();
        buf.pos(endX,   startY, z2).tex(1, 1).endVertex();
        buf.pos(startX, startY, z2).tex(0, 1).endVertex();

        // Lato sinistro
        buf.pos(startX, endY,  z2).tex(0, 0).endVertex();
        buf.pos(startX, endY,  z1).tex(1, 0).endVertex();
        buf.pos(startX, startY, z1).tex(1, 1).endVertex();
        buf.pos(startX, startY, z2).tex(0, 1).endVertex();

        // Lato destro
        buf.pos(endX,   endY,  z1).tex(0, 0).endVertex();
        buf.pos(endX,   endY,  z2).tex(1, 0).endVertex();
        buf.pos(endX,   startY, z2).tex(1, 1).endVertex();
        buf.pos(endX,   startY, z1).tex(0, 1).endVertex();

        tess.draw();
    }
}
