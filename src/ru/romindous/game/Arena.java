package ru.romindous.game;

import org.bukkit.*;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import ru.komiss77.ApiOstrov;
import ru.komiss77.enums.Game;
import ru.komiss77.enums.GameState;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.games.GM;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.modules.world.XYZ;
import ru.komiss77.scoreboard.SideBar;
import ru.komiss77.utils.*;
import ru.romindous.Main;
import ru.romindous.game.object.Build;
import ru.romindous.game.object.Nexus;
import ru.romindous.game.object.PlRusher;
import ru.romindous.game.object.Rusher;
import ru.romindous.game.object.build.Shop;
import ru.romindous.type.BuildType;
import ru.romindous.type.DirType;
import ru.romindous.type.RaceType;
import ru.romindous.type.Upgrade;

import java.util.*;
import java.util.function.Consumer;

public class Arena {

    public final static String AMT = "amt", LIMIT = "rem", GOLD = "gld", DUST = "dst";

    public final byte min;
    public final byte max;
    public final WXYZ lobby;
    public final String name;
    public final boolean bots;
    public final Shop[] shops;

    public final Set<Rusher> pls;
    public final Set<Rusher> spcs;

    public final Map<Integer, Build> blocs;
    public final Map<WXYZ, Nexus> orbs;

    private int time;
    public GameState gst;
    private BukkitTask task;

    public static final ItemStack sword = new ItemBuilder(Material.GOLDEN_SWORD).unbreak(true)
        .name("§4Каратель").lore(Arrays.asList(" ", "§7Сокруши своих §cврагов", "§4критическим §eударом")).build();
    public static final ItemStack pick = new ItemBuilder(Material.IRON_PICKAXE).unbreak(true)
        .name("§мКувалда").lore(Arrays.asList(" ", "§чЛКМ §e- Сокрушение", "§чПКМ §e- Строительство")).build();
    public static final ItemStack bow = new ItemBuilder(Material.BOW).unbreak(true).enchant(Enchantment.INFINITY)
        .name("§5Цибуля").lore(Arrays.asList(" ", "§7Напичкай своих §cврагов", "§7острыми §eмаслинами")).build();
    public static final ItemStack arrow = new ItemBuilder(Material.ARROW).name("§dМетка").build();
    public static final ItemStack map = new ItemBuilder(Material.COMPASS).name("§aКарта")
        .lore(Arrays.asList(" ", "§чПКМ §7- показать §eпостройки", "§7стой на §eоткрытой §7местности")).build();
    public static final ItemStack tpMap = new ItemBuilder(Material.RECOVERY_COMPASS).name("§2Магическая Карта")
        .lore(Arrays.asList(" ", "§чПКМ §7- показать §eпостройки", "§чЛКМ §7- §eтелепорт §7к ним")).build();
    private static final FireworkEffect fwe = FireworkEffect.builder().with(Type.BURST).flicker(true)
        .withColor(TCUtil.getBukkitColor(TCUtil.getTextColor(TCUtil.P))).withFade(TCUtil.getBukkitColor(TCUtil.getTextColor(TCUtil.A))).build();
    private static final char[] clrs = {'1', '2', '3', 'a', 'b', 'c', 'd', 'e'};

    public Arena(final String name, final byte min, final WXYZ lobby, final XYZ[] bases, final XYZ[] shops, final boolean bots) {
        this.min = min;
        this.name = name;
        this.lobby = lobby;
        this.bots = bots;
        this.gst = GameState.ОЖИДАНИЕ;
        this.pls = new HashSet<>();
        this.spcs = new HashSet<>();

        this.blocs = new HashMap<>();
        this.orbs = new HashMap<>();
        Main.shuffle(clrs, Main.srnd);
        this.max = (byte) Math.min(bases.length, clrs.length);
        for (int i = max - 1; i >= 0; i--) {
            this.orbs.put(new WXYZ(lobby.w, bases[i]), new Nexus(null, clrs[i]));
        }

        this.shops = new Shop[shops.length];
        for (int i = 0; i < shops.length; i++) {
            this.shops[i] = new Shop(this, new WXYZ(lobby.w, shops[i]));
        }

        this.task = null;
    }

    public boolean join(final Rusher rs) {
        switch (gst) {
            default:
                return spec(rs);
            case ФИНИШ:
                rs.getPlayer().sendMessage(Main.PRFX + "§cКарта " + TCUtil.P + name + " §cуже заканчивается!");
                return false;
            case ОЖИДАНИЕ, СТАРТ:
                if (pls.size() < max) {
                    rs.ifPlayer(p -> {
                        pls.add(rs);
                        rs.arena(this);
                        Main.nrmlzPl(p);
                        Main.inGameCnt();
                        rs.clearInv();
                        rs.item(Main.color, 2);
                        rs.item(Main.race, 4);
                        rs.item(Main.leave, 7);
                        p.teleport(lobby.getCenterLoc());
                        p.sendMessage(Main.PRFX + TCUtil.N + "Ты на карте " + TCUtil.P + name);
                        final String prm = ((PlRusher) rs).getTopPerm();
                        rs.taq(TCUtil.N + "[" + TCUtil.P + name + TCUtil.N + "] ", TCUtil.P,
                            prm.isEmpty() ? "" : TCUtil.N + " (§e" + prm + TCUtil.N + ")");
                        for (final Rusher r : pls) {
                            r.ifPlayer(rp -> {
                                ScreenUtil.sendActionBarDirect(rp, amtToHB());
                                if (rp.getEntityId() != p.getEntityId()) {
                                    rp.sendMessage(Main.PRFX + TCUtil.P + rs.name() + TCUtil.N + " зашел на карту!");
                                }
                            });
                        }
                        if (pls.size() == min) {
                            countStart();
                        } else if (pls.size() < min) {
                            GM.sendArenaData(Game.GR, this.name, GameState.ОЖИДАНИЕ, pls.size(), Main.PRFX, "", "", "");
                            for (final Rusher r : pls) {
                                r.ifPlayer(rp -> {
                                    if (rp.getEntityId() == p.getEntityId()) {
                                        startScore((PlRusher) r);
                                    } else {
                                        ((Oplayer) rs).score.getSideBar()
                                            .update(AMT, TCUtil.N + "Игроков: " + TCUtil.P + pls.size() + TCUtil.N + " чел.")
                                            .update(LIMIT, TCUtil.N + "Ждем еще " + TCUtil.P + (min - pls.size()) + TCUtil.N + " чел.");
                                    }
                                });
                            }
                        } else {
                            GM.sendArenaData(Game.GR, this.name, GameState.СТАРТ, pls.size(), Main.PRFX, "", "", "");
                            for (final Rusher r : pls) {
                                r.ifPlayer(rp -> {
                                    if (rp.getEntityId() == p.getEntityId()) {
                                        startScore((PlRusher) r);
                                    } else {
                                        ((Oplayer) rs).score.getSideBar()
                                            .update(AMT, TCUtil.N + "Игроков: " + TCUtil.P + pls.size() + TCUtil.N + " чел.");
                                    }
                                });
                            }
                        }
                    });
                } else {
                    rs.getPlayer().sendMessage(Main.PRFX + "§cКарта §6" + name + " §cзаполнена!");
                    return false;
                }
        }
        return true;
    }

    private boolean spec(final Rusher rs) {
        spcs.add(rs);
        rs.arena(this);
        Main.inGameCnt();
        rs.teleport(rs.getEntity(), lobby.getCenterLoc());
        rs.ifPlayer(p -> {
            p.setGameMode(GameMode.SPECTATOR);
            Main.inGameCnt();
            p.sendMessage(Main.PRFX + TCUtil.N + "Простмотр карты " + TCUtil.P + name);
            rs.taq(TCUtil.N + "[" + TCUtil.P + name + TCUtil.N + "] ",
                TCUtil.N, TCUtil.N + " (§8Зритель" + TCUtil.N + ")");
        });
        rs.clearInv();
        rs.item(Main.glow, 2);
        rs.item(Main.leave, 7);
        return true;
    }

    public void leave(final Rusher rs) {
        rs.ifPlayer(p -> {
            if (pls.remove(rs)) {
                switch (gst) {
                    case ОЖИДАНИЕ:
                        if (pls.size() == 0) end();
                        else {
                            GM.sendArenaData(Game.GR, this.name, GameState.ОЖИДАНИЕ, pls.size(), Main.PRFX, "", "", "");
                            for (final Rusher r : pls) {
                                r.ifPlayer(rp -> {
                                    ScreenUtil.sendActionBarDirect(rp, amtToHB());
                                    rp.sendMessage(Main.PRFX + TCUtil.P + rs.name() + TCUtil.N + " вышел с карты!");
                                });
                            }
                        }
                        break;
                    case СТАРТ:
                        if (pls.size() < min) {
                            if (task != null) {
                                task.cancel();
                                gst = GameState.ОЖИДАНИЕ;
                            }
                            for (final Rusher r : pls) {
                                r.ifPlayer(rp -> {
                                    ScreenUtil.sendActionBarDirect(rp, amtToHB());
                                    rp.sendMessage(Main.PRFX + TCUtil.P + rs.name() + TCUtil.N + " вышел с карты!");
                                    rp.sendMessage(Main.PRFX + TCUtil.N + "На карте недостаточно игроков для начала!");
                                    startScore((PlRusher) r);
                                });
                            }
                            GM.sendArenaData(Game.GR, this.name, GameState.ОЖИДАНИЕ, pls.size(), Main.PRFX, "", "", "");
                        } else {
                            for (final Rusher r : pls) {
                                r.ifPlayer(rp -> {
                                    ScreenUtil.sendActionBarDirect(rp, amtToHB());
                                    rp.sendMessage(Main.PRFX + TCUtil.P + rs.name() + TCUtil.N + " вышел с карты!");
                                    ((Oplayer) rs).score.getSideBar()
                                        .update(AMT, TCUtil.N + "Игроков: " + TCUtil.P + pls.size() + TCUtil.N + " чел.");
                                });
                            }
                            GM.sendArenaData(Game.GR, this.name, GameState.СТАРТ, pls.size(), Main.PRFX, "", "", "");
                        }
                        break;
                    case ИГРА:
                        killTeam(rs.team());
                        break;
                    case ФИНИШ:
                        if (pls.size() == 0) end();
                        break;
                    default:
                        break;
                }
                Main.inGameCnt();
            } else if (spcs.remove(rs)) {
                p.sendMessage(Main.PRFX + "Простмотр карты завершен!");
                Main.inGameCnt();
            }
            Main.lobbyPl(p, (PlRusher) rs);
        });
    }

    private void countStart() {
        time = 30;
        gst = GameState.СТАРТ;
        lobby.w.setFullTime(12000l);
        GM.sendArenaData(Game.GR, name, GameState.СТАРТ, pls.size(), Main.PRFX, "", "", "");

        for (final Rusher rs : pls) {
            rs.ifPlayer(p -> {
                startScore((PlRusher) rs);
            });
        }

        task = new BukkitRunnable() {
            @Override
            public void run() {
                switch (time--) {
                    case 1:
                        if (task != null) task.cancel();
                        countGame();
                        return;
                    default:
                        break;
                }

                final String rmn = TCUtil.P + TimeUtil.secondToTime(time) + TCUtil.N + "до начала!";
                for (final Rusher rs : pls) {
                    rs.ifPlayer(p -> {
                        ((Oplayer) rs).score.getSideBar().update(LIMIT, rmn);
                        if (time < 6) {
                            ScreenUtil.sendTitleDirect(p, (time < 4 ? TCUtil.A : TCUtil.P) + time, "");
                            p.playSound(p.getEyeLocation(), Sound.BLOCK_COPPER_BREAK, 2f, 0.8f);
                        }
                    });
                }
            }
        }.runTaskTimer(Main.plug, 0, 20);
    }

    private void countGame() {
        time = 3200;
        gst = GameState.ИГРА;
        lobby.w.setFullTime(12000l);
        for (final Entity le : lobby.w.getEntities()) {
            if (le instanceof LivingEntity) continue;
            le.remove();
        }
        GM.sendArenaData(Game.GR, name, GameState.ИГРА, pls.size(), Main.PRFX, "", "", "");

        for (final Rusher rs : pls) {
            final WXYZ org = getNr2DLoc(rs.getEntity().getLocation(), orbs.keySet());
            rs.team(new Nexus(rs, orbs.remove(org).cc));
            if (rs.race() == null) rs.race(ClassUtil.rndElmt(RaceType.values()));
            rs.team().blds.add(rs.race().build(rs.team(), org, BuildType.GOLD, 1));
            rs.team().chgRecs(100, 0);

            rs.clearInv();
            rs.item(sword, 0);
            rs.item(bow, 1);
            rs.item(pick, 2);
            rs.item(arrow, 9);
            rs.item(DirType.FOLLOW.icn, 8);
            rs.dir(DirType.FOLLOW);

            for (final Upgrade up : Upgrade.values())
                if (up.canBuy(rs, null)) up.addFor(rs);

            rs.teleport(rs.getEntity(), new Location(org.w, org.x + ApiOstrov.rndSignNum(BuildType.maxXZ, 3),
                org.y + 1, org.z + ApiOstrov.rndSignNum(BuildType.maxXZ, 3)));
            rs.taq(TCUtil.N + "[" + TCUtil.P + name + TCUtil.N + "] ", rs.team().color(),
                TCUtil.N + " (" + TCUtil.P + rs.klls() + TCUtil.N + "-" + TCUtil.P
                    + rs.mkls() + TCUtil.N + "-" + TCUtil.P + rs.dths() + TCUtil.N + ")");
        }

        for (final Rusher rs : pls) {
            rs.ifPlayer(p -> {
                Main.nrmlzPl(p);
                gameScore((PlRusher) rs);
                ScreenUtil.sendTitleDirect(p, TCUtil.N + "Начинаем", TCUtil.N + "Построй " + TCUtil.P + "армию " +
                    TCUtil.N + "и " + TCUtil.A + "сокруши " + TCUtil.N + "врагов!", 12, 60, 20);
                p.playSound(p.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 2f, 0.6f);
            });
        }

        orbs.clear();

        task = new BukkitRunnable() {
            @Override
            public void run() {
                switch (time--) {
                    case 120:
                        toAllPls(p -> ScreenUtil.sendActionBarDirect(p, TCUtil.N + "Осталось " + TCUtil.P + "2 минуты " + TCUtil.N + "до конца!"));
                        break;
                    case 60:
                        toAllPls(p -> ScreenUtil.sendActionBarDirect(p, TCUtil.N + "Осталась " + TCUtil.P + "1 минута " + TCUtil.N + "до конца!"));
                        break;
                    case 0:
                        countFinish();
                        return;
                    default:
                        break;
                }

                final String rmn = TCUtil.P + TimeUtil.secondToTime(time);
                lobby.w.setTime((time % 375) << 6);
                for (final Rusher rs : pls) {
                    final Nexus tm = rs.team();
                    if (!tm.alive) continue;

                    for (final Build b : tm.blds) b.act();

                    rs.ifPlayer(p -> {
                        ((Oplayer) rs).score.getSideBar().update(LIMIT, rmn);
                    });
                }
            }
        }.runTaskTimer(Main.plug, 0, 20);
    }

    public static WXYZ getNr2DLoc(final Location loc, final Collection<WXYZ> arr) {
        WXYZ lc = null;
        int dst = Integer.MAX_VALUE;
        final int X = loc.getBlockX(), Z = loc.getBlockZ();
        for (final WXYZ l : arr) {
            final int dx = l.x - X, dz = l.z - Z, dl = dx * dx + dz * dz;
            if (lc == null || dl < dst) {
                dst = dl;
                lc = l;
            }
        }
        return lc;
    }

    private void countFinish() {
        if (task != null) task.cancel();
        switch (pls.size()) {
            case 0:
                end();
                return;
            case 1:
                time = 10;
                gst = GameState.ФИНИШ;
                GM.sendArenaData(Game.GR, name, GameState.ФИНИШ, 1, Main.PRFX, "", "", "");
                final Rusher rs = pls.iterator().next();
                rs.ifPlayer(p -> {
                    ScreenUtil.sendTitleDirect(p, TCUtil.N + "Победа", TCUtil.N + "Комманда " +
                        TCUtil.nameOf(rs.team().cc, "ая", true) + " §eОдержала Верх!");
                    ApiOstrov.addStat(p, Stat.GR_game);
                    ApiOstrov.addStat(p, Stat.GR_win);
                });
                task = new BukkitRunnable() {
                    @Override
                    public void run() {
                        switch (time--) {
                            case 0:
                                if (task != null) task.cancel();
                                end();
                                return;
                            default:
                                final Firework fw = lobby.w.spawn(rs.getEntity().getLocation(), Firework.class);
                                final FireworkMeta fm = fw.getFireworkMeta();
                                fm.addEffect(fwe);
                                fw.setFireworkMeta(fm);
                                fm.setPower(2);
                                break;
                        }
                    }
                }.runTaskTimer(Main.plug, 0, 20);
                break;
            default:
                time = 10;
                gst = GameState.ФИНИШ;
                GM.sendArenaData(Game.GR, name, GameState.ФИНИШ, pls.size(), Main.PRFX, "", "", "");
                for (final Rusher r : pls) {
                    r.ifPlayer(p -> ScreenUtil.sendTitleDirect(p, TCUtil.A + "Ничья", "§cНикто не Сумел Одержать Верх!"));
                }
                task = new BukkitRunnable() {
                    @Override
                    public void run() {
                        switch (time--) {
                            case 0:
                                if (task != null) task.cancel();
                                end();
                                return;
                            default:
                                for (final Rusher r : pls) {
                                    final Firework fw = lobby.w.spawn(r.getEntity().getLocation(), Firework.class);
                                    final FireworkMeta fm = fw.getFireworkMeta();
                                    fm.addEffect(fwe);
                                    fw.setFireworkMeta(fm);
                                    fm.setPower(2);
                                }
                                break;
                        }
                    }
                }.runTaskTimer(Main.plug, 0, 20);
                break;
        }
    }

    public void addKill(final Rusher rs, final boolean plKll) {
        if (plKll) {
            rs.kllsI();
            rs.ifPlayer(p -> ApiOstrov.addStat(p, Stat.GR_kill));
        } else {
            rs.mklsI();
            rs.ifPlayer(p -> ApiOstrov.addStat(p, Stat.GR_pz));
        }
        rs.taq(TCUtil.N + "[" + TCUtil.P + name + TCUtil.N + "] ", rs.team().color(),
            TCUtil.N + " (" + TCUtil.P + rs.klls() + TCUtil.N + "-" + TCUtil.P
                + rs.mkls() + TCUtil.N + "-" + TCUtil.P + rs.dths() + TCUtil.N + ")");
    }

    public void killRs(final Rusher rs) {
        final LivingEntity le = rs.getEntity();
        if (gst == GameState.ИГРА) {
            rs.dthsI();
            rs.taq(TCUtil.N + "[" + TCUtil.P + name + TCUtil.N + "] ", rs.team().color(),
                TCUtil.N + " (" + TCUtil.P + rs.klls() + TCUtil.N + "-" + TCUtil.P
                    + rs.mkls() + TCUtil.N + "-" + TCUtil.P + rs.dths() + TCUtil.N + ")");

            rs.ifPlayer(p -> {
                p.closeInventory();
                ApiOstrov.addStat(p, Stat.GR_death);
                killPl(p);
            });

            final WXYZ rsp = rs.team().getCloseResp(new WXYZ(le.getLocation(), false), true);
            if (rsp == null) {
                killTeam(rs.team());
            } else {
                rs.teleport(le, rsp.getCenterLoc());
            }
        }
    }

    private void killPl(final Player p) {
        p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20d);
        p.setHealth(20d);
        p.setFireTicks(-1);
        p.setFreezeTicks(0);
    }

    public void killTeam(final Nexus nx) {
        nx.alive = false;
        for (final Build b : nx.blds) {
            b.remove(false);
        }
        nx.blds.clear();
        pls.remove(nx.rs);
        nx.rs.ifPlayer(p -> {
            ApiOstrov.addStat(p, Stat.GR_game);
            ApiOstrov.addStat(p, Stat.GR_loose);
            Main.lobbyPl(p, (PlRusher) nx.rs);
        });
        if (pls.size() == 1) {
            final Rusher wr = pls.iterator().next();
            if (wr instanceof PlRusher) {
                countFinish();
                return;
            }
            end();
            return;
        }
        for (final Rusher rs : pls) {
            rs.ifPlayer(p -> {
                ((Oplayer) rs).score.getSideBar()
                    .update(AMT, TCUtil.N + "Комманды:" + getTeamLifes());
            });
        }
    }

    //сколько игроков из скольки
    public String amtToHB() {
        return pls.size() < min ? TCUtil.N + "На карте " + TCUtil.P + pls.size() + TCUtil.N +
            " игроков, нужно еще " + TCUtil.A + (min - pls.size()) + TCUtil.N + " для начала"
            : TCUtil.N + "На карте " + TCUtil.P + pls.size() + TCUtil.N + " игроков, максимум: " + TCUtil.A + max;
    }

    private void startScore(final PlRusher rs) {
        final SideBar sb = rs.score.getSideBar().reset().title(Main.PRFX)
            .add(" ")
            .add(TCUtil.N + "Карта: " + TCUtil.P + name)
            .add(TCUtil.A + "=-=-=-=-=-=-=-")
            .add(AMT, TCUtil.N + "Игроков: " + TCUtil.P + pls.size() + TCUtil.N + " чел.")
            .add(" ")
            .add(TCUtil.A + "=-=-=-=-=-=-=-");
        if (gst == GameState.ОЖИДАНИЕ) {
            sb.add(LIMIT, TCUtil.N + "Ждем еще " + TCUtil.P + (min - pls.size()) + TCUtil.N + " чел.");
        } else {
            sb.add(LIMIT, TCUtil.P + TimeUtil.secondToTime(time) + TCUtil.N + "до начала!");
        }
        sb.add(" ").add("§e    ostrov77.ru").build();
    }

    private void gameScore(final PlRusher rs) {
        rs.score.getSideBar().reset().title(Main.PRFX)
            .add(" ")
            .add(TCUtil.N + "Карта: " + TCUtil.P + name)
            .add(TCUtil.A + "=-=-=-=-=-=-=-")
            .add(" ")
            .add(AMT, TCUtil.N + "Комманды:" + getTeamLifes())
            .add(TCUtil.N + "Цвет: " + TCUtil.nameOf(rs.team().cc, "ый", true))
            .add(" ")
            .add(GOLD, TCUtil.N + "Золото: " + TCUtil.P + rs.team().gold + " ⛃")
            .add(DUST, TCUtil.N + "Пыль: " + TCUtil.A + rs.team().dust + " 🔥")
            .add(" ")
            .add(TCUtil.A + "=-=-=-=-=-=-=-")
            .add(LIMIT, TCUtil.P + TimeUtil.secondToTime(time))
            .add(" ")
            .add("§e    ostrov77.ru").build();
    }

    public void end() {
        if (task != null) task.cancel();
        GM.sendArenaData(Game.GR, name, GameState.ОЖИДАНИЕ, 0, Main.PRFX, "", "", "");
        Main.active.remove(name);
        if (!pls.isEmpty()) {
            final Iterator<Rusher> it = pls.iterator();
            while (it.hasNext()) {
                final Rusher rs = it.next();
                for (final Build bd : rs.team().blds) {
                    bd.remove(false);
                }
                rs.ifPlayer(p -> {
                    Main.lobbyPl(p, (PlRusher) rs);
                });
                it.remove();
            }
        }

        if (!spcs.isEmpty()) {
            final Iterator<Rusher> it = spcs.iterator();
            while (it.hasNext()) {
                final Rusher rs = it.next();
                rs.ifPlayer(p -> Main.lobbyPl(p, (PlRusher) rs));
                it.remove();
            }
        }

        for (final Shop sh : shops) sh.remove(false);

        if (task != null) task.cancel();
    }

    private String getTeamLifes() {
        final StringBuilder sb = new StringBuilder(pls.size() << 3);
        for (final Rusher rs : pls) {
            sb.append(rs.team().alive ? " §r" + rs.team().color() + "⛨" : " §m" + rs.team().color() + "⛨");
        }
        return sb.toString();
    }

    private void toAllPls(final Consumer<Player> cn) {
        for (final Rusher rs : pls) rs.ifPlayer(cn);
    }
}
