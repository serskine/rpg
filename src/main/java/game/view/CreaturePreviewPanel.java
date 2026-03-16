package game.view;

import game.common.Creature;

import javax.swing.*;
import java.awt.*;

public class CreaturePreviewPanel extends JPanel {

    private Creature creature;

    public CreaturePreviewPanel() {
        setBackground(new Color(255, 250, 240));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    public void setCreature(Creature creature) {
        this.creature = creature;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (creature == null) {
            drawEmptyState(g);
            return;
        }
        drawCreatureStatBlock(g);
    }

    private void drawEmptyState(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(100, 100, 100));
        g2d.setFont(new Font("Serif", Font.ITALIC, 20));
        FontMetrics fm = g2d.getFontMetrics();
        String msg = "Select a creature to view details";
        g2d.drawString(msg, getWidth() / 2 - fm.stringWidth(msg) / 2, getHeight() / 2);
    }

    private void drawCreatureStatBlock(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int x = 15;
        int y = 25;
        int width = getWidth() - 40;

        // Name
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Serif", Font.BOLD, 26));
        g2d.drawString(creature.title, x, y);
        y += 32;

        // Title bar (now below name)
        g2d.setColor(new Color(139, 69, 19));
        g2d.fillRect(x, y, width, 3);
        y += 15;

        // Type line (size, type, alignment)
        g2d.setFont(new Font("Serif", Font.ITALIC, 16));
        String typeLine = creature.size + " " + creature.job + ", " + creature.alignment;
        g2d.drawString(typeLine, x, y);
        y += 28;

        // AC and HP
        g2d.setFont(new Font("Serif", Font.BOLD, 16));
        String hpText = "Hit Points " + creature.hp() + "/" + creature.maxHp();
        String acText = "Armor Class " + creature.ac();
        String acHpLine = acText + "  |  " + hpText;
        
        FontMetrics fm = g2d.getFontMetrics();
        int hpTextWidth = fm.stringWidth(hpText);
        int acTextWidth = fm.stringWidth(acText);
        
        g2d.drawString(acHpLine, x, y);
        y += 22;

        // HP bar - width matches HP text, positioned after "Armor Class X  |  "
        int hpBarX = x + acTextWidth + fm.stringWidth("  |  ");
        int hpBarWidth = hpTextWidth;
        int currentHpWidth = (int) ((double) creature.hp() / creature.maxHp() * hpBarWidth);
        g2d.setColor(new Color(200, 0, 0));
        g2d.fillRect(hpBarX, y - 16, currentHpWidth, 10);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(hpBarX, y - 16, hpBarWidth, 10);
        y += 25;

        // Speed
        g2d.setFont(new Font("Serif", Font.BOLD, 16));
        g2d.drawString("Speed: 30 ft.", x, y);
        y += 30;

        // Divider
        g2d.setColor(new Color(139, 69, 19));
        g2d.drawLine(x, y, x + width, y);
        y += 20;

        // Ability Scores
        g2d.setFont(new Font("Serif", Font.BOLD, 15));
        g2d.setColor(Color.BLACK);
        
        int str = creature.strength();
        int dex = creature.dexterity();
        int con = creature.constitution();
        int intel = creature.intelligence();
        int wis = creature.wisdom();
        int cha = creature.charisma();
        
        int strMod = game.util.Func.modifier(str);
        int dexMod = game.util.Func.modifier(dex);
        int conMod = game.util.Func.modifier(con);
        int intMod = game.util.Func.modifier(intel);
        int wisMod = game.util.Func.modifier(wis);
        int chaMod = game.util.Func.modifier(cha);
        
        String statsLine = String.format("STR %d (%+d)  DEX %d (%+d)  CON %d (%+d)  INT %d (%+d)  WIS %d (%+d)  CHA %d (%+d)",
            str, strMod, dex, dexMod, con, conMod, intel, intMod, wis, wisMod, cha, chaMod);
        g2d.drawString(statsLine, x, y);
        y += 30;

        // Divider
        g2d.setColor(new Color(139, 69, 19));
        g2d.drawLine(x, y, x + width, y);
        y += 20;

        // Saving Throws
        g2d.setFont(new Font("Serif", Font.BOLD, 14));
        g2d.setColor(Color.BLACK);
        int saveStr = strMod + creature.prof();
        int saveDex = dexMod + creature.prof();
        int saveCon = conMod + creature.prof();
        int saveInt = intMod + creature.prof();
        int saveWis = wisMod + creature.prof();
        int saveCha = chaMod + creature.prof();
        
        String savesLine = String.format("Saving Throws STR %+d, DEX %+d, CON %+d, INT %+d, WIS %+d, CHA %+d",
            saveStr, saveDex, saveCon, saveInt, saveWis, saveCha);
        g2d.drawString(savesLine, x, y);
        y += 24;

        // Skills (based on abilities)
        String skillsLine = String.format("Skills Athletics %+d, Acrobatics %+d, Stealth %+d, Perception %+d",
            strMod + creature.prof(), dexMod + creature.prof(), dexMod + creature.prof(), wisMod + creature.prof());
        g2d.drawString(skillsLine, x, y);
        y += 24;

        // Passive Perception
        g2d.drawString("Passive Perception " + creature.passivePerception(), x, y);
        y += 24;

        // Divider
        g2d.setColor(new Color(139, 69, 19));
        g2d.drawLine(x, y, x + width, y);
        y += 20;

        // Actions header
        g2d.setFont(new Font("Serif", Font.BOLD, 16));
        g2d.setColor(Color.BLACK);
        g2d.drawString("ACTIONS", x, y);
        y += 25;

        // Melee Attack
        g2d.setFont(new Font("Serif", Font.BOLD, 14));
        String attackName = "Melee Weapon Attack: ";
        int attackBonus = creature.meleeAttack();
        int damage = creature.hd();
        String attackLine = String.format("%s+%d to hit, reach 5 ft., one target. Hit: %d (%dd%d + %d) slashing damage.",
            attackName, attackBonus, damage, creature.level, 6, strMod);
        g2d.drawString(attackName, x, y);
        
        g2d.setFont(new Font("Serif", Font.PLAIN, 14));
        g2d.drawString(String.format("+%d to hit, reach 5 ft., one target. Hit: %d (%dd%d + %d) slashing damage.",
            attackBonus, damage, creature.level, 6, strMod), x + g2d.getFontMetrics().stringWidth(attackName), y);
        y += 24;

        // Ranged Attack
        g2d.setFont(new Font("Serif", Font.BOLD, 14));
        String rangedAttackName = "Ranged Weapon Attack: ";
        int rangedBonus = creature.rangedAttack();
        String rangedLine = String.format("%s+%d to hit, range 80/320 ft., one target. Hit: %d (%dd%d + %d) piercing damage.",
            rangedAttackName, rangedBonus, damage, creature.level, 8, dexMod);
        g2d.drawString(rangedAttackName, x, y);
        
        g2d.setFont(new Font("Serif", Font.PLAIN, 14));
        g2d.drawString(String.format("+%d to hit, range 80/320 ft., one target. Hit: %d (%dd%d + %d) piercing damage.",
            rangedBonus, damage, creature.level, 8, dexMod), x + g2d.getFontMetrics().stringWidth(rangedAttackName), y);
        y += 30;

        // Spellcasting (if applicable)
        if (creature.job.isCaster && creature.spellDc().isPresent()) {
            g2d.setColor(new Color(139, 69, 19));
            g2d.drawLine(x, y, x + width, y);
            y += 20;

            g2d.setFont(new Font("Serif", Font.BOLD, 16));
            g2d.setColor(Color.BLACK);
            g2d.drawString("SPELLCASTING", x, y);
            y += 25;

            g2d.setFont(new Font("Serif", Font.PLAIN, 14));
            String spellLine = String.format("Spellcasting DC %d, attack +%d", 
                creature.spellDc().get(), creature.spellAttack().orElse(0));
            g2d.drawString(spellLine, x, y);
            y += 24;
            
            g2d.setFont(new Font("Serif", Font.ITALIC, 13));
            g2d.drawString("1st level (3 slots): mage armor, magic missile,", x, y);
            y += 18;
            g2d.drawString("shield (at will: fire bolt, light)", x, y);
            y += 25;
        }

        // Bottom bar
        y += 15;
        g2d.setColor(new Color(139, 69, 19));
        g2d.fillRect(x, y, width, 3);
    }
}
