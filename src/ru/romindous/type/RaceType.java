package ru.romindous.type;

import ru.romindous.game.object.build.IBuild;
import ru.romindous.game.object.build.MBuild;
import org.bukkit.Material;
import org.bukkit.Sound;

import ru.romindous.game.object.Build;
import ru.romindous.game.object.Nexus;
import ru.romindous.game.object.build.PBuild;
import ru.komiss77.modules.world.WXYZ;

public enum RaceType {
	
	ILLAGER("Илладжеры", Material.REINFORCED_DEEPSLATE, "§ч", Sound.ENTITY_PILLAGER_AMBIENT, 1.0f, 1.2f, 1.2f),
	MONSTER("Монстры", Material.MOSSY_COBBLESTONE, "§о", Sound.ENTITY_ZOMBIE_VILLAGER_AMBIENT, 1.2f, 1.25f, 1.0f),
	PIGLIN("Пиглины", Material.RED_NETHER_BRICKS, "§к", Sound.ENTITY_PIGLIN_BRUTE_AMBIENT, 1.25f, 1.4f, 1.25f),
	;

	public final String name;
	public final Material icn;
	public final String clr;
	public final Sound snd;
	public final float mcst;
	public final float mcd;
	public final float mbhp;
	
	RaceType(final String name, final Material icn, final String clr,
             final Sound snd, final float mcst, final float mcd, final float mbhp) {
		this.name = name;
		this.icn = icn;
		this.clr = clr;
		this.snd = snd;
		this.mcst = mcst;
		this.mcd = mcd;
		this.mbhp = mbhp;
	}

	public Build build(final Nexus nx, final WXYZ lc, final BuildType to, final int lvl) {
		return switch (this) {
		case ILLAGER -> new IBuild(nx, lc, to, lvl);
		case MONSTER -> new MBuild(nx, lc, to, lvl);
		case PIGLIN -> new PBuild(nx, lc, to, lvl);
        };
	}

}
