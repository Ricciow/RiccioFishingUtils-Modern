package cloud.glitchdev.rfu.constants

enum class Dyes(val dyeName: String, val hex: String, val relatedScs : List<SeaCreatures> = listOf()) {
    AQUAMARINE("Aquamarine Dye", "7FFFD4", SeaCreatures.entries.filter { it.liquidType == LiquidTypes.WATER }),
    ARCHFIEND("Archfiend Dye", "B80036"),
    BINGO_BLUE("Bingo Blue Dye", "002FA7"),
    BONE("Bone Dye", "E3DAC9"),
    BRICK_RED("Brick Red Dye", "CB4154"),
    BYZANTIUM("Byzantium Dye", "702963"),
    CARMINE("Carmine Dye", "960018", SeaCreatures.entries.filter { it.liquidType == LiquidTypes.LAVA }),
    CELADON("Celadon Dye", "ACE1AF"),
    CELESTE("Celeste Dye", "B2FFFF"),
    CHOCOLATE("Chocolate Dye", "7B3F00"),
    COPPER("Copper Dye", "B87333"),
    CYCLAMEN("Cyclamen Dye", "F56FA1"),
    DARK_PURPLE("Dark Purple Dye", "301934"),
    DUNG("Dung Dye", "4F2A2A"),
    EMERALD("Emerald Dye", "50C878"),
    FLAME("Flame Dye", "E25822"),
    FOSSIL("Fossil Dye", "866F12"),
    FROSTBITTEN("Frostbitten Dye", "09D8EB"),
    HOLLY("Holly Dye", "3C6746"),
    ICEBERG("Iceberg Dye", "71A6D2", SeaCreatures.entries.filter { it.category == SeaCreatureCategory.WINTER }),
    JADE("Jade Dye", "00A86B"),
    LIVID("Livid Dye", "CEB7AA"),
    MANGO("Mango Dye", "FDBE02"),
    MATCHA("Matcha Dye", "74A12E"),
    MIDNIGHT("Midnight Dye", "50216C"),
    MOCHA("Mocha Dye", "967969"),
    MYTHOLOGICAL("Mythological Dye", "6F6F0C"),
    NADESHIKO("Nadeshiko Dye", "F6ADC6"),
    NECRON("Necron Dye", "E7413C"),
    NYANZA("Nyanza Dye", "E9FFDB"),
    PEARLESCENT("Pearlescent Dye", "115555"),
    PELT("Pelt Dye", "50414C"),
    PERIWINKLE("Periwinkle Dye", "CCCCFF"),
    PURE_BLACK("Pure Black Dye", "000000"),
    PURE_BLUE("Pure Blue Dye", "0013FF"),
    PURE_WHITE("Pure White Dye", "FFFFFF"),
    PURE_YELLOW("Pure Yellow Dye", "FFF700"),
    SANGRIA("Sangria Dye", "D40808"),
    SECRET("Secret Dye", "7D7D7D"),
    TENTACLE("Tentacle Dye", "324D6C"),
    TREASURE("Treasure Dye", "FCD12A"),
    WILD_STRAWBERRY("Wild Strawberry Dye", "FF43A4");

    override fun toString(): String {
        return dyeName
    }

    companion object {
        fun getRelatedDye(dye : String) : Dyes? {
            return entries.find { it.dyeName == dye }
        }
    }
}