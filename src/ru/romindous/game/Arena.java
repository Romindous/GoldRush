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
import ru.komiss77.enums.GameState;
import ru.komiss77.enums.Stat;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.modules.world.XYZ;
import ru.komiss77.scoreboard.SideBar;
import ru.komiss77.utils.ItemBuilder;
import ru.komiss77.utils.TCUtils;
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

    public static final ItemStack sword = new ItemBuilder(Material.GOLDEN_SWORD).setUnbreakable(true)
            .name("§4Каратель").addLore(Arrays.asList(" ", "§7Сокруши своих §cврагов", "§4критическим §eударом")).build();
    public static final ItemStack pick = new ItemBuilder(Material.IRON_PICKAXE).setUnbreakable(true)
            .name("§мКувалда").addLore(Arrays.asList(" ", "§чЛКМ §e- Сокрушение", "§чПКМ §e- Строительство")).build();
    public static final ItemStack bow = new ItemBuilder(Material.BOW).setUnbreakable(true).addEnchant(Enchantment.ARROW_INFINITE)
            .name("§5Цибуля").addLore(Arrays.asList(" ", "§7Напичкай своих §cврагов", "§7острыми §eмаслинами")).build();
    public static final ItemStack arrow = new ItemBuilder(Material.ARROW).name("§dМетка").build();
    public static final ItemStack map = new ItemBuilder(Material.COMPASS).name("§aКарта")
            .addLore(Arrays.asList(" ", "§чПКМ §7- показать §eпостройки", "§7стой на §eоткрытой §7местности")).build();
    public static final ItemStack tpMap = new ItemBuilder(Material.RECOVERY_COMPASS).name("§2Магическая Карта")
            .addLore(Arrays.asList(" ", "§чПКМ §7- показать §eпостройки", "§чЛКМ §7- §eтелепорт §7к ним")).build();
    private static final FireworkEffect fwe = FireworkEffect.builder().with(Type.BURST).flicker(true)
            .withColor(TCUtils.getBukkitColor(TCUtils.getTextColor(TCUtils.P))).withFade(TCUtils.getBukkitColor(TCUtils.getTextColor(TCUtils.A))).build();
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
                rs.getPlayer().sendMessage(Main.PRFX + "§cКарта " + TCUtils.P + name + " §cуже заканчивается!");
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
                        p.sendMessage(Main.PRFX + TCUtils.N + "Ты на карте " + TCUtils.P + name);
                        final String prm = ((PlRusher) rs).getTopPerm();
                        rs.taq(TCUtils.N + "[" + TCUtils.P + name + TCUtils.N + "] ", TCUtils.P,
                                prm.isEmpty() ? "" : TCUtils.N + " (§e" + prm + TCUtils.N + ")");
                        for (final Rusher r : pls) {
                            r.ifPlayer(rp -> {
                                ApiOstrov.sendActionBarDirect(rp, amtToHB());
                                if (rp.getEntityId() != p.getEntityId()) {
                                    rp.sendMessage(Main.PRFX + TCUtils.P + rs.name() + TCUtils.N + " зашел на карту!");
                                }
                            });
                        }
                        if (pls.size() == min) {
                            countStart();
                        } else if (pls.size() < min) {
                            ApiOstrov.sendArenaData(this.name, GameState.ОЖИДАНИЕ, Main.PRFX, "", "", "", "", pls.size());
                            for (final Rusher r : pls) {
                                r.ifPlayer(rp -> {
                                    if (rp.getEntityId() == p.getEntityId()) {
                                        startScore((PlRusher) r);
                                    } else {
                                        ((Oplayer) rs).score.getSideBar()
                                                .update(AMT, TCUtils.N + "Игроков: " + TCUtils.P + pls.size() + TCUtils.N + " чел.")
                                                .update(LIMIT, TCUtils.N + "Ждем еще " + TCUtils.P + (min - pls.size()) + TCUtils.N + " чел.");
                                    }
                                });
                            }
                        } else {
                            ApiOstrov.sendArenaData(this.name, GameState.СТАРТ, Main.PRFX, "", "", "", "", pls.size());
                            for (final Rusher r : pls) {
                                r.ifPlayer(rp -> {
                                    if (rp.getEntityId() == p.getEntityId()) {
                                        startScore((PlRusher) r);
                                    } else {
                                        ((Oplayer) rs).score.getSideBar()
                                                .update(AMT, TCUtils.N + "Игроков: " + TCUtils.P + pls.size() + TCUtils.N + " чел.");
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
            p.sendMessage(Main.PRFX + TCUtils.N + "Простмотр карты " + TCUtils.P + name);
            rs.taq(TCUtils.N + "[" + TCUtils.P + name + TCUtils.N + "] ",
                    TCUtils.N, TCUtils.N + " (§8Зритель" + TCUtils.N + ")");
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
                            ApiOstrov.sendArenaData(this.name, GameState.ОЖИДАНИЕ, Main.PRFX, "", "", "", "", pls.size());
                            for (final Rusher r : pls) {
                                r.ifPlayer(rp -> {
                                    ApiOstrov.sendActionBarDirect(rp, amtToHB());
                                    rp.sendMessage(Main.PRFX + TCUtils.P + rs.name() + TCUtils.N + " вышел с карты!");
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
                                    ApiOstrov.sendActionBarDirect(rp, amtToHB());
                                    rp.sendMessage(Main.PRFX + TCUtils.P + rs.name() + TCUtils.N + " вышел с карты!");
                                    rp.sendMessage(Main.PRFX + TCUtils.N + "На карте недостаточно игроков для начала!");
                                    startScore((PlRusher) r);
                                });
                            }
                            ApiOstrov.sendArenaData(this.name, GameState.ОЖИДАНИЕ, Main.PRFX, "", "", "", "", pls.size());
                        } else {
                            for (final Rusher r : pls) {
                                r.ifPlayer(rp -> {
                                    ApiOstrov.sendActionBarDirect(rp, amtToHB());
                                    rp.sendMessage(Main.PRFX + TCUtils.P + rs.name() + TCUtils.N + " вышел с карты!");
                                    ((Oplayer) rs).score.getSideBar()
                                            .update(AMT, TCUtils.N + "Игроков: " + TCUtils.P + pls.size() + TCUtils.N + " чел.");
                                });
                            }
                            ApiOstrov.sendArenaData(this.name, GameState.СТАРТ, Main.PRFX, "", "", "", "", pls.size());
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
        ApiOstrov.sendArenaData(name, GameState.СТАРТ, Main.PRFX, "", "", "", "", pls.size());

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

                final String rmn = TCUtils.P + ApiOstrov.secondToTime(time) + TCUtils.N + "до начала!";
                for (final Rusher rs : pls) {
                    rs.ifPlayer(p -> {
                        ((Oplayer) rs).score.getSideBar().update(LIMIT, rmn);
                        if (time < 6) {
                            ApiOstrov.sendTitleDirect(p, (time < 4 ? TCUtils.A : TCUtils.P) + time, "");
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
        ApiOstrov.sendArenaData(name, GameState.ИГРА, Main.PRFX, "", "", "", "", pls.size());

        for (final Rusher rs : pls) {
            final WXYZ org = getNr2DLoc(rs.getEntity().getLocation(), orbs.keySet());
            rs.team(new Nexus(rs, orbs.remove(org).cc));
            if (rs.race() == null) rs.race(ApiOstrov.rndElmt(RaceType.values()));
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
            rs.taq(TCUtils.N + "[" + TCUtils.P + name + TCUtils.N + "] ", rs.team().color(),
                    TCUtils.N + " (" + TCUtils.P + rs.klls() + TCUtils.N + "-" + TCUtils.P
                            + rs.mkls() + TCUtils.N + "-" + TCUtils.P + rs.dths() + TCUtils.N + ")");
        }

        for (final Rusher rs : pls) {
            rs.ifPlayer(p -> {
                Main.nrmlzPl(p);
                gameScore((PlRusher) rs);
                ApiOstrov.sendTitleDirect(p, TCUtils.N + "Начинаем", TCUtils.N + "Построй " + TCUtils.P + "армию " +
                        TCUtils.N + "и " + TCUtils.A + "сокруши " + TCUtils.N + "врагов!", 12, 60, 20);
                p.playSound(p.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 2f, 0.6f);
            });
        }

        orbs.clear();

        task = new BukkitRunnable() {
            @Override
            public void run() {
                switch (time--) {
                    case 120:
                        toAllPls(p -> ApiOstrov.sendActionBarDirect(p, TCUtils.N + "Осталось " + TCUtils.P + "2 минуты " + TCUtils.N + "до конца!"));
                        break;
                    case 60:
                        toAllPls(p -> ApiOstrov.sendActionBarDirect(p, TCUtils.N + "Осталась " + TCUtils.P + "1 минута " + TCUtils.N + "до конца!"));
                        break;
                    case 0:
                        countFinish();
                        return;
                    default:
                        break;
                }

                final String rmn = TCUtils.P + ApiOstrov.secondToTime(time);
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
                ApiOstrov.sendArenaData(name, GameState.ФИНИШ, Main.PRFX, "", "", "", "", 1);
                final Rusher rs = pls.iterator().next();
                rs.ifPlayer(p -> {
                    ApiOstrov.sendTitleDirect(p, TCUtils.N + "Победа", TCUtils.N + "Комманда " +
                            TCUtils.nameOf(rs.team().cc, "ая", true) + " §eОдержала Верх!");
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
                ApiOstrov.sendArenaData(name, GameState.ФИНИШ, Main.PRFX, "", "", "", "", pls.size());
                for (final Rusher r : pls) {
                    r.ifPlayer(p -> ApiOstrov.sendTitleDirect(p, TCUtils.A + "Ничья", "§cНикто не Сумел Одержать Верх!"));
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
        rs.taq(TCUtils.N + "[" + TCUtils.P + name + TCUtils.N + "] ", rs.team().color(),
                TCUtils.N + " (" + TCUtils.P + rs.klls() + TCUtils.N + "-" + TCUtils.P
                        + rs.mkls() + TCUtils.N + "-" + TCUtils.P + rs.dths() + TCUtils.N + ")");
    }

    public void killRs(final Rusher rs) {
        final LivingEntity le = rs.getEntity();
        if (gst == GameState.ИГРА) {
            rs.dthsI();
            rs.taq(TCUtils.N + "[" + TCUtils.P + name + TCUtils.N + "] ", rs.team().color(),
                    TCUtils.N + " (" + TCUtils.P + rs.klls() + TCUtils.N + "-" + TCUtils.P
                            + rs.mkls() + TCUtils.N + "-" + TCUtils.P + rs.dths() + TCUtils.N + ")");

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
                        .update(AMT, TCUtils.N + "Комманды:" + getTeamLifes());
            });
        }
    }

    //сколько игроков из скольки
    public String amtToHB() {
        return pls.size() < min ? TCUtils.N + "На карте " + TCUtils.P + pls.size() + TCUtils.N +
                " игроков, нужно еще " + TCUtils.A + (min - pls.size()) + TCUtils.N + " для начала"
                : TCUtils.N + "На карте " + TCUtils.P + pls.size() + TCUtils.N + " игроков, максимум: " + TCUtils.A + max;
    }

    private void startScore(final PlRusher rs) {
        final SideBar sb = rs.score.getSideBar().reset().title(Main.PRFX)
                .add(" ")
                .add(TCUtils.N + "Карта: " + TCUtils.P + name)
                .add(TCUtils.A + "=-=-=-=-=-=-=-")
                .add(AMT, TCUtils.N + "Игроков: " + TCUtils.P + pls.size() + TCUtils.N + " чел.")
                .add(" ")
                .add(TCUtils.A + "=-=-=-=-=-=-=-");
        if (gst == GameState.ОЖИДАНИЕ) {
            sb.add(LIMIT, TCUtils.N + "Ждем еще " + TCUtils.P + (min - pls.size()) + TCUtils.N + " чел.");
        } else {
            sb.add(LIMIT, TCUtils.P + ApiOstrov.secondToTime(time) + TCUtils.N + "до начала!");
        }
        sb.add(" ").add("§e    ostrov77.ru").build();
    }

    private void gameScore(final PlRusher rs) {
        rs.score.getSideBar().reset().title(Main.PRFX)
                .add(" ")
                .add(TCUtils.N + "Карта: " + TCUtils.P + name)
                .add(TCUtils.A + "=-=-=-=-=-=-=-")
                .add(" ")
                .add(AMT, TCUtils.N + "Комманды:" + getTeamLifes())
                .add(TCUtils.N + "Цвет: " + TCUtils.nameOf(rs.team().cc, "ый", true))
                .add(" ")
                .add(GOLD, TCUtils.N + "Золото: " + TCUtils.P + rs.team().gold + " ⛃")
                .add(DUST, TCUtils.N + "Пыль: " + TCUtils.A + rs.team().dust + " 🔥")
                .add(" ")
                .add(TCUtils.A + "=-=-=-=-=-=-=-")
                .add(LIMIT, TCUtils.P + ApiOstrov.secondToTime(time))
                .add(" ")
                .add("§e    ostrov77.ru").build();
    }

    public void end() {
        if (task != null) task.cancel();
        ApiOstrov.sendArenaData(name, GameState.ОЖИДАНИЕ, Main.PRFX, "", "", "", "", 0);
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
