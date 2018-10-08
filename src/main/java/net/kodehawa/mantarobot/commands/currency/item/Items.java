/*
 * Copyright (C) 2016-2018 David Alejandro Rubio Escares / Kodehawa
 *
 * Mantaro is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Mantaro is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Mantaro.  If not, see http://www.gnu.org/licenses/
 */

package net.kodehawa.mantarobot.commands.currency.item;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.kodehawa.mantarobot.commands.currency.item.special.FishRod;
import net.kodehawa.mantarobot.commands.currency.profile.Badge;
import net.kodehawa.mantarobot.core.modules.commands.i18n.I18nContext;
import net.kodehawa.mantarobot.data.MantaroData;
import net.kodehawa.mantarobot.db.ManagedDatabase;
import net.kodehawa.mantarobot.db.entities.DBUser;
import net.kodehawa.mantarobot.db.entities.Player;
import net.kodehawa.mantarobot.db.entities.helpers.Inventory;
import net.kodehawa.mantarobot.db.entities.helpers.PlayerData;
import net.kodehawa.mantarobot.utils.RandomCollection;
import net.kodehawa.mantarobot.utils.commands.EmoteReference;
import net.kodehawa.mantarobot.utils.commands.RateLimiter;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.kodehawa.mantarobot.utils.Utils.handleDefaultRatelimit;

@Slf4j
public class Items {
    public static final Item HEADPHONES, BAN_HAMMER, KICK_BOOT, FLOPPY_DISK, MY_MATHS, PING_RACKET,
            LOADED_DICE, FORGOTTEN_MUSIC, CC_PENCIL, OVERFLOWED_BAG, BROM_PICKAXE, MILK, ALCOHOL, LEWD_MAGAZINE, RING,
            LOOT_CRATE_KEY, BOOSTER, BERSERK, ENHANCER, RING_2, COMPANION, LOADED_DICE_2, LOVE_LETTER, CLOTHES, SHOES, DIAMOND, CHOCOLATE, COOKIES,
            NECKLACE, ROSE, DRESS, TUXEDO, LOOT_CRATE, STAR, STAR_2, SLOT_COIN, HOUSE, CAR, BELL_SPECIAL, CHRISTMAS_TREE_SPECIAL, PANTS, POTION_HASTE, POTION_CLEAN,
            POTION_STAMINA, FISHING_ROD, FISH_1, FISH_2, FISH_3, GEM_1, GEM_2, GEM_3, GEM_4, MOP, CLAIM_KEY, COFFEE, WAIFU_PILL, FISHING_BAIT, DIAMOND_PICKAXE,
            TELEVISION, WRENCH, MOTORCYCLE, GEM1_PICKAXE, GEM2_PICKAXE, PIZZA, GEM_5, GEM5_PICKAXE, MINE_CRATE, FISH_CRATE, FISH_PREMIUM_CRATE, MINE_PREMIUM_CRATE,
            GEM1_ROD, GEM2_ROD, GEM5_ROD, GEM5_PICKAXE_2, GEM5_2, GEM5_ROD_2, FISH_4, FISH_5;

    private static final Random r = new Random();
    private static final RateLimiter lootCrateRatelimiter = new RateLimiter(TimeUnit.MINUTES, 4);

    public static final Item[] ALL = {
            HEADPHONES = new Item(ItemType.COLLECTABLE, "\uD83C\uDFA7", "Headphones", "items.headphones", "items.description.headphones", 5, true, false, false),
            BAN_HAMMER = new Item(ItemType.COLLECTABLE, "\uD83D\uDD28", "Ban Hammer", "items.ban_hammer", "items.description.ban_hammer", 15, false, false),
            KICK_BOOT = new Item(ItemType.COLLECTABLE, "\uD83D\uDC62", "Kick Boot",  "items.kick_boot","items.description.kick_boot", 12, false, false),
            FLOPPY_DISK = new Item(ItemType.COLLECTABLE, "\uD83D\uDCBE", "Floppy Disk", "items.floppy", "items.description.floppy", 13, false, false),
            MY_MATHS = new Item(ItemType.COLLECTABLE, "\uD83D\uDCDD", "My Maths", "items.maths", "items.description.maths", 11, false, false),
            PING_RACKET = new Item(ItemType.COLLECTABLE, "\uD83C\uDFD3", "Ping Racket", "items.racket", "items.description.racket", 15, false, false),
            LOADED_DICE = new Item(ItemType.COLLECTABLE, "\uD83C\uDFB2", "Loaded Die", "items.die", "items.description.loaded_die", 45, false, false),
            FORGOTTEN_MUSIC = new Item(ItemType.COLLECTABLE, "\uD83C\uDFB5", "Forgotten Music", "items.forgotten", "items.description.forgotten", 15, false, false),
            CC_PENCIL = new Item(ItemType.COLLECTABLE, "\u270f", "Pencil", "items.pencil", "items.description.pencil", 15, false, false),
            OVERFLOWED_BAG = new Item(ItemType.COLLECTABLE, "\uD83D\uDCB0","Moneybag", "items.moneybag", "items.description.moneybag", 95, true),
            BROM_PICKAXE = new Item(ItemType.INTERACTIVE, "\u26cf","Brom's Pickaxe", "items.pick", "items.description.pick", 100, true),
            MILK = new Item(ItemType.COMMON, EmoteReference.POTION1.getUnicode(),"Milk", "items.milk", "items.description.milk", 25, true),
            ALCOHOL = new Item(ItemType.COMMON, EmoteReference.POTION2.getUnicode(),"Beverage", "items.beverage", "items.description.beverage", 25, true),
            LEWD_MAGAZINE = new Item(ItemType.COMMON, EmoteReference.MAGAZINE.getUnicode(),"Lewd Magazine", "items.lewd", "items.description.lewd", 25, true),
            RING = new Item(ItemType.COMMON, EmoteReference.RING.getUnicode(),"Marriage Ring", "items.ring", "items.description.ring", 60, true),
            LOVE_LETTER = new Item(ItemType.COLLECTABLE, EmoteReference.LOVE_LETTER.getUnicode(),"Love Letter", "items.letter", "items.description.letter", 45, false, false),
            LOOT_CRATE_KEY = new Item(ItemType.COMMON, EmoteReference.KEY.getUnicode(),"Crate Key", "items.key", "items.description.key", 58, true),
            CLOTHES = new Item(ItemType.COMMON, EmoteReference.CLOTHES.getUnicode(),"Clothes", "items.clothes", "items.description.clothes", 30, true),
            DIAMOND = new Item(ItemType.COMMON, EmoteReference.DIAMOND.getUnicode(),"Diamond", "items.diamond", "items.description.diamond", 350, true),
            DRESS = new Item(ItemType.COMMON, EmoteReference.DRESS.getUnicode(),"Wedding Dress", "items.dress", "items.description.dress", 75, true),
            NECKLACE = new Item(ItemType.COMMON, EmoteReference.NECKLACE.getUnicode(),"Necklace", "items.necklace", "items.description.necklace", 17, true),
            TUXEDO = new Item(ItemType.COMMON, EmoteReference.TUXEDO.getUnicode(),"Tuxedo", "items.tuxedo", "items.description.tuxedo", 50, true),
            SHOES = new Item(ItemType.COMMON, EmoteReference.SHOES.getUnicode(),"Shoes", "items.shoes", "items.description.shoes", 10, true),
            ROSE = new Item(ItemType.COMMON, EmoteReference.ROSE.getUnicode(),"Rose", "items.rose", "items.description.rose", 25, true),
            CHOCOLATE = new Item(ItemType.COMMON, EmoteReference.CHOCOLATE.getUnicode(),"Chocolate", "items.chocolate", "items.description.chocolate", 23, true),
            COOKIES = new Item(ItemType.COMMON, EmoteReference.COOKIE.getUnicode(),"Cookie", "items.cookie", "items.description.cookie", 10, true),

            // ---------------------------------- LEFT OVERS FROM CURRENCY V1 STARTS HERE ----------------------------------
            //CANNOT REMOVE BECAUSE WE WERE MEME ENOUGH TO FUCKING SAVE THEM BY THEIR IDS
            LOADED_DICE_2 = new Item("\uD83C\uDFB2","Special Loaded Die", "items.description.die_2"),
            BOOSTER = new Item(EmoteReference.RUNNER.getUnicode(),"Booster", "items.description.booster"),
            BERSERK = new Item(EmoteReference.CROSSED_SWORD.getUnicode(),"Berserk", "items.description.berserk"),
            COMPANION = new Item(EmoteReference.DOG.getUnicode(),"Companion", "items.description.companion"),
            RING_2 = new Item("\uD83D\uDC5A","Special Ring", "items.description.special_ring"),
            ENHANCER = new Item(EmoteReference.MAG.getUnicode(),"Enchancer", "items.description.enchancer"),
            STAR = new Item(ItemType.COLLECTABLE, "\uE335","Prize", "items.prize", "items.description.prize", 0, false, false, true),

            // ---------------------------------- LEFT OVERS FROM CURRENCY V1 END HERE ----------------------------------
            LOOT_CRATE = new Item(ItemType.CRATE, EmoteReference.LOOT_CRATE.getDiscordNotation(),"Loot Crate",  "items.crate","items.description.crate", 0, false, false, true, (event, context) -> openLootCrate(event, context.getLeft(), ItemType.LootboxType.RARE, 33, EmoteReference.LOOT_CRATE, 3)),
            STAR_2 = new Item(ItemType.COMMON, EmoteReference.STAR.getUnicode(),"Prize 2", "items.prize_2", "items.description.prize_2", 500, true, false, true),
            SLOT_COIN = new Item(ItemType.COMMON, "\uD83C\uDF9F","Slot ticket", "items.slot_ticket","items.description.slot_ticket", 65, true, true),
            HOUSE = new Item(ItemType.COMMON, EmoteReference.HOUSE.getUnicode(), "House", "items.house", "items.description.house", 5000, true, true),
            CAR = new Item(ItemType.COMMON, "\uD83D\uDE97","Car",  "items.car","items.description.car", 1000, true, true),

            // ---------------------------------- CHRISTMAS 2017 EVENT STARTS HERE ----------------------------------
            BELL_SPECIAL = new Item(ItemType.RARE, "\uD83D\uDD14","Christmas bell", "items.bell", "items.description.bell", 0, false, false, true),
            CHRISTMAS_TREE_SPECIAL = new Item(ItemType.RARE, "\uD83C\uDF84", "Christmas tree", "items.tree", "items.description.tree", 0, false, false, true),
            // ---------------------------------- CHRISTMAS 2017 EVENT ENDS HERE ----------------------------------

            // ---------------------------------- 5.0 ITEMS START HERE ----------------------------------
            PANTS = new Item(ItemType.COMMON, "\uD83D\uDC56", "Pants", "items.pants", "items.description.pants", 20, true),
            POTION_HASTE = new Item(ItemType.INTERACTIVE, "\uD83C\uDF76","Haste Potion", "items.haste", "items.description.haste", 490, true),
            POTION_CLEAN = new Item(ItemType.INTERACTIVE, "\uD83C\uDF7C","Milky Potion", "items.milky", "items.description.milky", 50, true),
            POTION_STAMINA = new Item(ItemType.INTERACTIVE, "\uD83C\uDFFA","Energy Beverage", "items.energy", "items.description.energy", 450, true),
            FISHING_ROD = new FishRod(ItemType.INTERACTIVE, 3, "\uD83C\uDFA3","Fishing Rod", "items.rod", "items.description.rod", 65, true, "", 0),
            FISH_1 = new Item(ItemType.FISHING, "\uD83D\uDC1F","Fish", "items.fish", "items.description.fish", 10, false),
            FISH_2 = new Item(ItemType.FISHING, "\uD83D\uDC20","Tropical Fish", "items.tropical_fish", "items.description.tropical_fish", 30, false),
            FISH_3 = new Item(ItemType.FISHING, "\uD83D\uDC21","Blowfish", "items.blowfish", "items.description.blowfish", 15, false),
            // ---------------------------------- 5.0 MINING ITEMS START HERE ----------------------------------
            GEM_1 = new Item(ItemType.CAST_OBTAINABLE, "\u2604", "Comet Gem", "items.comet_gem", "items.description.comet_gem", 40, true, false, "3;1", 51, 24),
            GEM_2 = new Item(ItemType.CAST_OBTAINABLE, EmoteReference.STAR.getUnicode(), "Star Gem", "items.star_gem", "items.description.star_gem", 60, true, false, "4;1", 51, 25),
            GEM_3 = new Item(ItemType.MINE, "\uD83D\uDD78", "Cobweb", "items.cobweb", "items.description.cobweb", 10, false),
            GEM_4 = new Item(ItemType.MINE, "\uD83D\uDCAB", "Gem Fragment", "items.fragment", "items.description.fragment", 50, false),
            // ---------------------------------- 5.0 ITEMS START HERE (again lol) ----------------------------------
            MOP = new Item(ItemType.INTERACTIVE, "\u3030","Mop", "items.mop", "items.description.mop", 10, true),
            CLAIM_KEY = new Item(ItemType.COMMON, "\uD83D\uDDDD","Claim Key", "items.claim_key", "items.description.claim_key", 1, false, true),
            COFFEE = new Item(ItemType.COMMON, "\u2615","Coffee", "items.coffee", "items.description.coffee", 10, true),
            WAIFU_PILL = new Item(ItemType.INTERACTIVE, "\ud83d\udc8a","Waifu Pill", "items.waifu_pill", "items.description.waifu_pill", 670, true),
            FISHING_BAIT = new Item(ItemType.INTERACTIVE, "\uD83D\uDC1B","Fishing Bait", "items.bait", "items.description.bait", 15, true),
            DIAMOND_PICKAXE = new Item(ItemType.CAST_MINE, EmoteReference.DIAMOND_PICK.getDiscordNotation(),"Diamond Pickaxe", "items.diamond_pick", "items.description.diamond_pick", 450, true, false, "1;2", 10, 18),
            TELEVISION = new Item(ItemType.COMMON, "\uD83D\uDCFA","Television", "items.tv", "items.description.tv", 45, true),
            WRENCH = new Item(ItemType.COMMON, "\ud83d\udd27","Wrench", "items.wrench", "items.description.wrench", 50, true),
            //car is 1000 credits, so this is 350
            MOTORCYCLE = new Item(ItemType.COMMON, "\uD83C\uDFCD","Motorcycle",  "items.motorcycle","items.description.motorcycle", 350, true),
            GEM1_PICKAXE = new Item(ItemType.CAST_MINE, EmoteReference.COMET_PICK.getDiscordNotation(),"Comet Pickaxe", "items.comet_pick", "items.description.comet_pick", 350, true, false, "1;2", 10, 48),
            GEM2_PICKAXE = new Item(ItemType.CAST_MINE, EmoteReference.STAR_PICK.getDiscordNotation(),"Star Pickaxe", "items.star_pick", "items.description.star_pick", 350, true, false, "1;2", 10, 49),
            PIZZA = new Item(ItemType.COMMON, "\uD83C\uDF55","Pizza", "items.pizza", "items.description.pizza", 15, true, false),
            GEM_5 = new Item(ItemType.COMMON, "\u2728", "Sparkle Fragment", "general.deprecated", "general.deprecated", 0, false, false),
            GEM5_PICKAXE = new Item(ItemType.COMMON, "\u26cf","Broken Sparkle Pickaxe", "general.deprecated", "general.deprecated", 550, true, false),
            MINE_CRATE = new Item(ItemType.CRATE, EmoteReference.MINE_CRATE.getDiscordNotation(),"Gem Crate",  "items.mine_crate","items.description.mine_crate", 0, false, false, true,  (event, context) -> openLootCrate(event, context.getLeft(), ItemType.LootboxType.MINE, 66, EmoteReference.MINE_CRATE, 3)),
            FISH_CRATE = new Item(ItemType.CRATE, EmoteReference.FISH_CRATE.getDiscordNotation(),"Fish Treasure",  "items.fish_crate","items.description.fish_crate", 0, false, false, true,  (event, context) -> openLootCrate(event, context.getLeft(), ItemType.LootboxType.FISH, 67, EmoteReference.FISH_CRATE, 3)),
            FISH_PREMIUM_CRATE = new Item(ItemType.CRATE, EmoteReference.PREMIUM_FISH_CRATE.getDiscordNotation(),"Fish Premium Treasure",  "items.fish_premium_crate","items.description.fish_premium_crate", 0, false, false, true, (event, context) -> openLootCrate(event, context.getLeft(), ItemType.LootboxType.FISH_PREMIUM, 68, EmoteReference.FISH_CRATE, 5)),
            MINE_PREMIUM_CRATE = new Item(ItemType.CRATE, EmoteReference.PREMIUM_MINE_CRATE.getDiscordNotation(),"Gem Premium Crate",  "items.mine_premium_crate","items.description.mine_premium_crate", 0, false, false, true, (event, context) -> openLootCrate(event, context.getLeft(), ItemType.LootboxType.MINE_PREMIUM, 69, EmoteReference.MINE_CRATE, 5)),
            GEM1_ROD = new FishRod(ItemType.CAST_FISH, 6, EmoteReference.COMET_ROD.getDiscordNotation(),"Comet Gem Rod", "items.comet_rod", "items.description.comet_rod", 65, "1;3", 44, 48),
            GEM2_ROD = new FishRod(ItemType.CAST_FISH, 9, EmoteReference.STAR_ROD.getDiscordNotation(),"Star Gem Rod", "items.star_rod", "items.description.star_rod", 65, "1;3", 44, 49),
            GEM5_ROD = new FishRod(ItemType.COMMON, 3, "\uD83C\uDFA3","Broken Sparkle Rod", "general.deprecated", "general.deprecated", 65, "",2),
            GEM5_PICKAXE_2 = new Item(ItemType.MINE_RARE_PICK, EmoteReference.SPARKLE_PICK.getDiscordNotation(),"Sparkle Pickaxe", "items.sparkle_pick", "items.description.sparkle_pick", 2550, true, false, "1;4;1", 10, 74, 18),
            GEM5_2 = new Item(ItemType.MINE_RARE, "\u2728", "Sparkle Fragment", "items.sparkle", "items.description.sparkle", 605, false),
            GEM5_ROD_2 = new FishRod(ItemType.CAST_FISH, 14, EmoteReference.SPARKLE_ROD.getDiscordNotation(),"Sparkle Rod", "items.sparkle_rod", "items.description.sparkle_rod", 65, "1;4;1", 44, 74, 18),
            FISH_4 = new Item(ItemType.FISHING_RARE, "\uD83D\uDC1A","Shell", "items.shell", "items.description.shell", 1150, false),
            FISH_5 = new Item(ItemType.FISHING_RARE, "\uD83E\uDD88","Shark", "items.shark", "items.description.shark", 600, false),
    };


    public static void setItemActions() {
        final SecureRandom random = new SecureRandom();
        log.info("Registering item actions...");
        final ManagedDatabase managedDatabase = MantaroData.db();

        MOP.setAction(((event, ctx) -> {
            Player p = managedDatabase.getPlayer(event.getAuthor());
            DBUser dbUser = managedDatabase.getUser(event.getAuthor());
            I18nContext lang = ctx.getLeft();

            Inventory playerInventory = p.getInventory();
            if(!playerInventory.containsItem(MOP))
                return false;

            event.getChannel().sendMessageFormat(lang.get("general.misc_item_usage.mop"), EmoteReference.DUST).queue();
            playerInventory.process(new ItemStack(MOP, -1));
            p.save();
            dbUser.getData().setDustLevel(0);
            dbUser.save();
            return true;
        }));

        //Basically fish command.
        FISHING_ROD.setAction((event, context) -> {
            Player p = managedDatabase.getPlayer(event.getAuthor());
            DBUser u = managedDatabase.getUser(event.getAuthor());
            Inventory playerInventory = p.getInventory();

            I18nContext lang = context.getLeft();
            String itemString = context.getRight();

            //casting bc old stuff :clap:, the FishRod object is so we can handle level and stuff w/o that many issues
            FishRod item = (FishRod) FISHING_ROD;
            if(!itemString.isEmpty()) {
                Optional<Item> opt = Items.fromAnyNoId(itemString);
                Item i = opt.orElse(FISHING_ROD); //default to normal rod again if it doesn't properly find a fitting item
                if(!(i instanceof FishRod)) {
                    event.getChannel().sendMessageFormat(lang.get("commands.fish.not_suitable"), EmoteReference.ERROR).queue();
                    return true;
                }

                item = (FishRod) i;
            }

            //Level but starting at 0.
            int nominalLevel = item.getLevel() - 3;
            String extraMessage = "";

            if(!playerInventory.containsItem(item))
                return false;

            //Rod break ratio is as follows: with stamina it's break ratio (73 + (level + 4)) plus 7 more, while without stamina is just the break ratio.
            int breakRatio = item.getBreakRatio();
            if(r.nextInt(100) > (handlePotion(POTION_STAMINA, 4, p) ? breakRatio + 7 : breakRatio)) {
                //Your rod is done for, rip.
                event.getChannel().sendMessageFormat(lang.get("commands.fish.rod_broke"), EmoteReference.SAD).queue();
                //Remove the item from the player inventory.
                playerInventory.process(new ItemStack(item, -1));
                p.save();
                return false;
            } else {
                int select = random.nextInt(100);

                if(select < 10) {
                    //Here your fish rod got dusty. Yes, on the sea.
                    int level = u.getData().increaseDustLevel(r.nextInt(4));
                    event.getChannel().sendMessageFormat(lang.get("commands.fish.dust"), EmoteReference.TALKING, level).queue();
                    u.save();
                    return false;
                } else if(select < 35) {
                    //Here you found trash.
                    List<Item> common = Stream.of(ALL)
                            .filter(i -> i.getItemType() == ItemType.COMMON && !i.isHidden() && i.isSellable() && i.value < 45)
                            .collect(Collectors.toList());

                    Item selected = common.get(random.nextInt(common.size()));
                    if(playerInventory.getAmount(selected) >= 5000) {
                        event.getChannel().sendMessageFormat(lang.get("commands.fish.trash.overflow"), EmoteReference.SAD).queue();
                        return true;
                    }

                    playerInventory.process(new ItemStack(selected, 1));
                    event.getChannel().sendMessageFormat(lang.get("commands.fish.trash.success"), EmoteReference.EYES, selected.getEmoji()).queue();
                } else {
                    //Here you actually caught fish, congrats.
                    List<Item> fish = Stream.of(ALL)
                            .filter(i -> i.getItemType() == ItemType.FISHING && !i.isHidden() && i.isSellable())
                            .collect(Collectors.toList());
                    RandomCollection<Item> fishItems = new RandomCollection<>();

                    int money = 0;
                    int amount = handleBuff(FISHING_BAIT, 1, p) ? Math.max(1, random.nextInt(item.getLevel() + 4)) : Math.max(1, random.nextInt(item.getLevel()));
                    fish.forEach((i1) -> fishItems.add(3, i1));

                    //Basically more chance if you have a better rod.
                    if (select > (75 - nominalLevel)) {
                        money = Math.max(5, random.nextInt(85 + (3 * nominalLevel)));
                    }

                    //START OF WAIFU HELP IMPLEMENTATION
                    boolean waifuHelp = false;
                    if (Items.handlePotion(Items.WAIFU_PILL, 5, p)) {
                        if (u.getData().getWaifus().entrySet().stream().anyMatch((w) -> w.getValue() > 10_000_000L)) {
                            money += Math.max(10, random.nextInt(100));
                            waifuHelp = true;
                        }
                    }
                    //END OF WAIFU HELP IMPLEMENTATION

                    //START OF FISH LOOT CRATE HANDLING
                    if (r.nextInt(400) > 380) {
                        Item crate = u.isPremium() ? Items.FISH_PREMIUM_CRATE : Items.FISH_CRATE;
                        if (playerInventory.getAmount(crate) >= 5000) {
                            extraMessage += "\n" + lang.get("commands.fish.crate.overflow");
                        } else {
                            playerInventory.process(new ItemStack(crate, 1));
                            extraMessage += "\n" + EmoteReference.MEGA + String.format(lang.get("commands.fish.crate.success"), crate.getEmoji(), crate.getName());
                        }
                    }
                    //END OF FISH LOOT CRATE HANDLING

                    //START OF ITEM ADDING HANDLING
                    List<ItemStack> list = new ArrayList<>(amount);
                    boolean overflow = false;
                    for (int i = 0; i < amount; i++) {
                        Item it = fishItems.next();
                        if (playerInventory.getAmount(it) >= 5000) {
                            overflow = true;
                            continue;
                        }

                        list.add(new ItemStack(it, 1));
                    }

                    if(overflow) {
                        extraMessage += "\n" + String.format(lang.get("commands.fish.overflow"), EmoteReference.SAD);
                    }

                    List<ItemStack> reducedList = ItemStack.reduce(list);
                    playerInventory.process(reducedList);
                    p.addMoney(money);

                    String itemDisplay = ItemStack.toString(reducedList);
                    boolean foundFish = !reducedList.isEmpty();
                    //END OF ITEM ADDING HANDLING

                    //Add fisher badge if the player found fish succesfully.
                    if (foundFish) {
                        p.getData().addBadgeIfAbsent(Badge.FISHER);
                    }

                    if(item == GEM5_ROD_2 && r.nextInt(30) > 25) {
                        playerInventory.process(new ItemStack(FISH_5, 1));
                        extraMessage += "\n" + EmoteReference.MEGA + String.format(lang.get("commands.fish.shark_success"), FISH_5.getEmoji());
                    }

                    if(nominalLevel >= 3 && r.nextInt(110) > 96) {
                        playerInventory.process(new ItemStack(FISH_4, 1));
                        extraMessage += "\n" + EmoteReference.MEGA + String.format(lang.get("commands.fish.fossil_success"), FISH_4.getEmoji());
                    }

                    //START OF REPLY HANDLING
                    //Didn't find a thingy thing.
                    if(money == 0 && !foundFish) {
                        int level = u.getData().increaseDustLevel(r.nextInt(4));
                        event.getChannel().sendMessageFormat(lang.get("commands.fish.dust"), EmoteReference.TALKING, level).queue();
                        u.save();
                        return false;
                    }

                    //if there's money, but not fish
                    if (money > 0 && !foundFish) {
                        event.getChannel().sendMessageFormat(lang.get("commands.fish.success_money_noitem") + extraMessage, item.getEmoji(), money).queue();
                    } else if (foundFish && money == 0) { //there's fish, but no money
                        event.getChannel().sendMessageFormat(lang.get("commands.fish.success") + extraMessage, item.getEmoji(), itemDisplay).queue();
                    } else if (money > 0 && foundFish) { //there's money and fish
                        event.getChannel().sendMessageFormat(lang.get("commands.fish.success_money") + extraMessage,
                                item.getEmoji(), itemDisplay, money, (waifuHelp ? "\n" + lang.get("commands.fish.waifu_help") : "")
                        ).queue();
                    }
                    //END OF REPLY HANDLING
                }

                //Save all changes to the player object.
                p.save();
                return true;
            }
        });

        //START OF PICKAXE ACTION DECLARATION
        BROM_PICKAXE.setAction((event, ctx) -> {
            Player p = managedDatabase.getPlayer(event.getAuthor());
            I18nContext lang = ctx.getLeft();
            return handlePickaxe(event, lang, BROM_PICKAXE, p, 0.29f); //29%
        });

        DIAMOND_PICKAXE.setAction((event, ctx) -> {
            Player p = managedDatabase.getPlayer(event.getAuthor());
            I18nContext lang = ctx.getLeft();
            return handlePickaxe(event, lang, DIAMOND_PICKAXE, p, 0.23f); //23%
        });

        //comet
        GEM1_PICKAXE.setAction((event, ctx) -> {
            Player p = managedDatabase.getPlayer(event.getAuthor());
            I18nContext lang = ctx.getLeft();
            return handlePickaxe(event, lang, GEM1_PICKAXE, p, 0.21f); //21%
        });

        //star
        GEM2_PICKAXE.setAction((event, ctx) -> {
            Player p = managedDatabase.getPlayer(event.getAuthor());
            I18nContext lang = ctx.getLeft();
            return handlePickaxe(event, lang, GEM2_PICKAXE, p, 0.15f); //15%
        });

        //sparkle
        GEM5_PICKAXE_2.setAction((event, ctx) -> {
            Player p = managedDatabase.getPlayer(event.getAuthor());
            I18nContext lang = ctx.getLeft();
            return handlePickaxe(event, lang, GEM5_PICKAXE_2, p, 0.07f); //7%
        });
        //END OF PICKAXE ACTION DECLARATION

        POTION_CLEAN.setAction((event, ctx) -> {
            I18nContext lang = ctx.getLeft();
            Player p = managedDatabase.getPlayer(event.getAuthor());
            p.getData().setActivePotion(null);
            event.getChannel().sendMessageFormat(lang.get("general.misc_item_usage.milk"), EmoteReference.CORRECT).queue();
            p.getInventory().process(new ItemStack(POTION_CLEAN, -1));
            p.save();
            return true;
        });

        POTION_STAMINA.setAction((event, ctx) -> {
            I18nContext lang = ctx.getLeft();
            Player p = managedDatabase.getPlayer(event.getAuthor());
            if(p.getData().getActivePotion() != null && p.getData().getActivePotion().getPotion() == idOf(POTION_STAMINA)) {
                event.getChannel().sendMessageFormat(lang.get("general.misc_item_usage.stamina_used"), EmoteReference.ERROR).queue();
                return false;
            }

            p.getData().setActivePotion(new PotionEffect(idOf(POTION_STAMINA), System.currentTimeMillis(), ItemType.PotionType.PLAYER));
            event.getChannel().sendMessageFormat(lang.get("general.misc_item_usage.stamina"), EmoteReference.CORRECT).queue();
            p.getInventory().process(new ItemStack(POTION_STAMINA, -1));
            p.save();
            return true;
        });

        POTION_HASTE.setAction((event, ctx) -> {
            I18nContext lang = ctx.getLeft();
            Player p = managedDatabase.getPlayer(event.getAuthor());
            if(p.getData().getActivePotion() != null && p.getData().getActivePotion().getPotion() == idOf(POTION_HASTE)) {
                event.getChannel().sendMessageFormat(lang.get("general.misc_item_usage.haste_used"), EmoteReference.ERROR).queue();
                return false;
            }


            p.getData().setActivePotion(new PotionEffect(idOf(POTION_HASTE),
                    System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(2), ItemType.PotionType.PLAYER));
            event.getChannel().sendMessageFormat(lang.get("general.misc_item_usage.haste"), EmoteReference.CORRECT).queue();
            p.getInventory().process(new ItemStack(POTION_HASTE, -1));
            p.save();
            return true;
        });

        FISHING_BAIT.setAction((event, ctx) -> {
            I18nContext lang = ctx.getLeft();
            Player p = managedDatabase.getPlayer(event.getAuthor());
            if(p.getData().getActiveBuff() != null && p.getData().getActiveBuff().getPotion() == idOf(FISHING_BAIT)) {
                event.getChannel().sendMessageFormat(lang.get("general.misc_item_usage.bait_used"), EmoteReference.POPPER).queue();
                return false;
            }

            p.getData().setActiveBuff(new PotionEffect(idOf(FISHING_BAIT),
                    System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(2), ItemType.PotionType.PLAYER));
            event.getChannel().sendMessageFormat(lang.get("general.misc_item_usage.bait"), EmoteReference.POPPER).queue();
            p.getInventory().process(new ItemStack(FISHING_BAIT, -1));
            p.save();
            return true;
        });

        WAIFU_PILL.setAction((event, ctx) -> {
            I18nContext lang = ctx.getLeft();
            Player p = managedDatabase.getPlayer(event.getAuthor());
            if(p.getData().getActiveBuff() != null && p.getData().getActiveBuff().getPotion() == idOf(WAIFU_PILL)) {
                event.getChannel().sendMessageFormat(lang.get("general.misc_item_usage.pill_used"), EmoteReference.ERROR).queue();
                return false;
            }

            p.getData().setActiveBuff(new PotionEffect(idOf(WAIFU_PILL),
                    System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(2), ItemType.PotionType.PLAYER));
            event.getChannel().sendMessageFormat(lang.get("general.misc_item_usage.pill"), EmoteReference.CORRECT).queue();
            p.getInventory().process(new ItemStack(WAIFU_PILL, -1));
            p.save();
            return true;
        });
    }

    public static Optional<Item> fromAny(String any) {
        try {
            Item item = fromId(Integer.parseInt(any));

            if(item != null)
                return Optional.of(item);
        } catch(NumberFormatException ignored) {}

        return fromAnyNoId(any);
    }

    public static Optional<Item> fromAnyNoId(String any) {
        Optional<Item> itemOptional;

        itemOptional = fromEmoji(any);
        if(itemOptional.isPresent())
            return itemOptional;

        itemOptional = fromName(any);
        if(itemOptional.isPresent())
            return itemOptional;

        itemOptional = fromPartialName(any);
        return itemOptional;
    }

    public static Optional<Item> fromEmoji(String emoji) {
        return Stream.of(ALL).filter(item -> item.getEmoji().equals(emoji)).findFirst();
    }

    public static Item fromId(int id) {
        return ALL[id];
    }

    public static Optional<Item> fromName(String name) {
        return Arrays.stream(ALL).filter(item -> item.getName().toLowerCase().trim().equals(name.toLowerCase().trim())).findFirst();
    }

    public static Optional<Item> fromPartialName(String name) {
        return Arrays.stream(ALL).filter(item -> item.getName().toLowerCase().trim().contains(name.toLowerCase().trim())).findFirst();
    }

    public static int idOf(Item item) {
        return Arrays.asList(ALL).indexOf(item);
    }

    private static boolean openLootCrate(GuildMessageReceivedEvent event, I18nContext lang, ItemType.LootboxType type, int item, EmoteReference typeEmote, int bound) {
        Player player = MantaroData.db().getPlayer(event.getAuthor());
        Inventory inventory = player.getInventory();
        Item crate = fromId(item);
        if(inventory.containsItem(crate)) {
            if(inventory.containsItem(LOOT_CRATE_KEY)) {
                if(!handleDefaultRatelimit(lootCrateRatelimiter, event.getAuthor(), event))
                    return false;

                inventory.process(new ItemStack(LOOT_CRATE_KEY, -1));
                inventory.process(new ItemStack(crate, -1));

                if(crate == LOOT_CRATE)
                    player.getData().addBadgeIfAbsent(Badge.THE_SECRET);
                player.save();

                openLootBox(event, lang, type, typeEmote, bound);
                return true;
            } else {
                event.getChannel().sendMessageFormat(lang.get("general.misc_item_usage.crate.no_key"), EmoteReference.ERROR).queue();
                return false;
            }
        } else {
            event.getChannel().sendMessageFormat(lang.get("general.misc_item_usage.crate.no_crate"), EmoteReference.ERROR).queue();
            return false;
        }
    }

    private static void openLootBox(GuildMessageReceivedEvent event, I18nContext lang, ItemType.LootboxType type, EmoteReference typeEmote, int bound) {
        List<Item> toAdd = selectItems(r.nextInt(bound) + bound, type);
        Player player = MantaroData.db().getPlayer(event.getAuthor());
        ArrayList<ItemStack> ita = new ArrayList<>();

        toAdd.forEach(item -> ita.add(new ItemStack(item, 1)));

        boolean overflow = player.getInventory().merge(ita);
        player.saveAsync();

        event.getChannel().sendMessage(String.format(lang.get("general.misc_item_usage.crate.success"),
                typeEmote.getDiscordNotation() + " ", toAdd.stream().map(item -> item.getEmoji() + " " + item.getName()).collect(Collectors.joining(", ")),
                overflow ? ". " + lang.get("general.misc_item_usage.crate.overflow") : "")).queue();
    }

    //Maybe compact this a bit? works fine, just icks me a bit.
    private static List<Item> selectItems(int amount, ItemType.LootboxType type) {
        List<Item> common = handleItemDrop(i -> i.getItemType() == ItemType.COMMON);
        List<Item> rare = handleItemDrop(i -> i.getItemType() == ItemType.RARE);
        List<Item> premium = handleItemDrop(i -> i.getItemType() == ItemType.PREMIUM);

        List<Item> mine = handleItemDrop(i -> i.getItemType() == ItemType.MINE || i.getItemType() == ItemType.CAST_OBTAINABLE);
        List<Item> fish = handleItemDrop(i -> i.getItemType() == ItemType.FISHING);

        List<Item> premiumMine = handleItemDrop(i -> i.getItemType() == ItemType.CAST_MINE ||
                i.getItemType() == ItemType.MINE || i.getItemType() == ItemType.MINE_RARE || i.getItemType() == ItemType.CAST_OBTAINABLE || i.getItemType() == ItemType.MINE_RARE_PICK);
        List<Item> premiumFish = handleItemDrop(i -> i.getItemType() == ItemType.CAST_FISH ||
                i.getItemType() == ItemType.FISHING || i.getItemType() == ItemType.FISHING_RARE);

        RandomCollection<Item> items = new RandomCollection<>();

        switch(type) {
            case PREMIUM:
                premium.forEach(i-> items.add(2, i));
            case RARE:
                rare.forEach(i-> items.add(5, i));
            case COMMON:
                common.forEach(i-> items.add(20, i));
                break; //fallthrough intended until here.
            case FISH_PREMIUM:
                premiumFish.forEach(i -> items.add(5, i));
                break;
            case MINE_PREMIUM:
                premiumMine.forEach(i -> items.add(5, i));
                break;
            case MINE:
                mine.forEach(i -> items.add(8, i));
                break;
            case FISH:
                fish.forEach(i -> items.add(8, i));
        }

        List<Item> list = new ArrayList<>(amount);
        for(int i = 0; i < amount; i++) {
            list.add(items.next());
        }

        return list;
    }

    private static List<Item> handleItemDrop(Predicate<Item> predicate) {
        List<Item> all = Arrays.stream(Items.ALL).filter(i->i.isBuyable() || i.isSellable()).collect(Collectors.toList());

        return all.stream().filter(predicate)
                .sorted(Comparator.comparingLong(i->i.value))
                .collect(Collectors.toList());
    }

    public static boolean handlePotion(Item i, int maxTimes, Player p) {
        final PlayerData playerData = p.getData();
        boolean isPotionPresent = playerData.getActivePotion() != null && fromId(playerData.getActivePotion().getPotion()) == i;
        if (isPotionPresent) {
            //counter starts at 0
            if (playerData.getActivePotion().getTimesUsed() >= maxTimes) {
                playerData.setActivePotion(null);
                p.save();
            } else {
                long timesUsed = playerData.getActivePotion().getTimesUsed();
                playerData.getActivePotion().setTimesUsed(timesUsed + 1);
                p.save();
            }
        }

        return isPotionPresent;
    }

    public static boolean handleBuff(Item i, int maxTimes, Player p) {
        final PlayerData playerData = p.getData();
        boolean isBuffPresent = playerData.getActiveBuff() != null && fromId(playerData.getActiveBuff().getPotion()) == i;
        if (isBuffPresent) {
            //counter starts at 0
            if (playerData.getActiveBuff().getTimesUsed() >= maxTimes) {
                playerData.setActiveBuff(null);
                p.save();
            } else {
                long timesUsed = playerData.getActiveBuff().getTimesUsed();
                playerData.getActiveBuff().setTimesUsed(timesUsed + 1);
                p.save();
            }
        }

        return isBuffPresent;
    }

    private static boolean handlePickaxe(GuildMessageReceivedEvent event, I18nContext lang, Item item, Player player, float chance) {
        Inventory playerInventory = player.getInventory();

        //Defensive programming :D
        if(!playerInventory.containsItem(item))
            return false;

        if(r.nextFloat() < (handlePotion(POTION_STAMINA, 4, player) ? (chance) - 0.07 : chance)) {
            event.getChannel().sendMessageFormat(lang.get("commands.mine.pick_broke"), EmoteReference.SAD).queue();
            playerInventory.process(new ItemStack(item, -1));
            player.save();
            return false;
        } else {
            return true;
        }

    }
}
