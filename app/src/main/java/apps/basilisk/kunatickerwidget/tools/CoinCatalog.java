package apps.basilisk.kunatickerwidget.tools;

import java.util.ArrayList;

import apps.basilisk.kunatickerwidget.R;

public class CoinCatalog {
    private ArrayList<Coin> listCoins;
    private static CoinCatalog instance;

    private CoinCatalog() {
        listCoins = new ArrayList<>();
        listCoins.add(new Coin("uah", "Hryvnia", R.drawable.icon_coin_uah));
        listCoins.add(new Coin("ada", "Cardano", R.drawable.icon_coin_ada));
        listCoins.add(new Coin("arn", "AERON", R.drawable.icon_coin_arn));
        listCoins.add(new Coin("bch", "BITCOIN-CASH", R.drawable.icon_coin_bch));
        listCoins.add(new Coin("btc", "BITCOIN", R.drawable.icon_coin_btc));
        listCoins.add(new Coin("btg", "BITCOIN-GOLD", R.drawable.icon_coin_btg));
        listCoins.add(new Coin("dash", "DASH", R.drawable.icon_coin_dash));
        listCoins.add(new Coin("doge", "DOGECOIN", R.drawable.icon_coin_doge));
        listCoins.add(new Coin("eos", "EOS", R.drawable.icon_coin_eos));
        listCoins.add(new Coin("erc20", "ERC20", R.drawable.icon_coin_erc20));
        listCoins.add(new Coin("etc", "ETHEREUM-CLASSIC", R.drawable.icon_coin_etc));
        listCoins.add(new Coin("eth", "ETHEREUM", R.drawable.icon_coin_eth));
        listCoins.add(new Coin("evr", "EVERUS", R.drawable.icon_coin_evr));
        listCoins.add(new Coin("eurs", "STASIS EURS", R.drawable.icon_coin_eurs));
        listCoins.add(new Coin("fno", "FONERO", R.drawable.icon_coin_fno));
        listCoins.add(new Coin("food", "FOOD", R.drawable.icon_coin_food));
        listCoins.add(new Coin("gbg", "GOLOS-GOLD", R.drawable.icon_coin_gbg));
        listCoins.add(new Coin("gol", "GOLOS", R.drawable.icon_coin_gol));
        listCoins.add(new Coin("hkn", "HACKEN", R.drawable.icon_coin_hkn));
        listCoins.add(new Coin("iota", "IOTA", R.drawable.icon_coin_iota));
        listCoins.add(new Coin("iti", "iTicoin", R.drawable.icon_coin_iti));
        listCoins.add(new Coin("krb", "KARBO", R.drawable.icon_coin_krb));
        listCoins.add(new Coin("kun", "KUN", R.drawable.icon_coin_kun));
        listCoins.add(new Coin("ltc", "LITECOIN", R.drawable.icon_coin_ltc));
        listCoins.add(new Coin("neo", "NEO", R.drawable.icon_coin_neo));
        listCoins.add(new Coin("nvc", "NOVACOIN", R.drawable.icon_coin_nvc));
        listCoins.add(new Coin("r", "REVAIN", R.drawable.icon_coin_r));
        listCoins.add(new Coin("rem", "REMME", R.drawable.icon_coin_rem));
        listCoins.add(new Coin("rmc", "RUS.-MINING-COIN", R.drawable.icon_coin_rmc));
        listCoins.add(new Coin("sib", "SIBCOIN", R.drawable.icon_coin_sib));
        listCoins.add(new Coin("tlr", "TALER", R.drawable.icon_coin_tlr));
        listCoins.add(new Coin("tusd", "TRUEUSD", R.drawable.icon_coin_tusd));
        listCoins.add(new Coin("venus", "VENUS", R.drawable.icon_coin_venus));
        listCoins.add(new Coin("waves", "WAVES", R.drawable.icon_coin_waves));
        listCoins.add(new Coin("xem", "NEM", R.drawable.icon_coin_xem));
        listCoins.add(new Coin("xlm", "STELLAR", R.drawable.icon_coin_xlm));
        listCoins.add(new Coin("xmr", "MONERO", R.drawable.icon_coin_xmr));
        listCoins.add(new Coin("xrp", "RIPPLE", R.drawable.icon_coin_xrp));
        listCoins.add(new Coin("zec", "ZCASH", R.drawable.icon_coin_zec));
        listCoins.add(new Coin("", "unknown", R.drawable.icon_coin_unknown));
    }
    
    public static CoinCatalog Instance() {
        if (instance == null) {
            instance = new CoinCatalog();
        }
        return instance;
    }

    public Coin getCoinInfo(String symbol) {
        Coin coin = null;
        for (int i = 0; i < listCoins.size(); i++) {
            coin = listCoins.get(i);
            if (coin.getSymbol().equalsIgnoreCase(symbol)) break;
        }
        if (coin.getName().equalsIgnoreCase("unknown")) coin.setName(symbol.toUpperCase());
        return coin;
    }

    public class Coin {
        private String symbol;
        private String name;
        private int iconRes;

        public Coin(String symbol, String name, int iconRes) {
            this.symbol = symbol;
            this.name = name;
            this.iconRes = iconRes;
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getIconRes() {
            return iconRes;
        }

        public void setIconRes(int iconRes) {
            this.iconRes = iconRes;
        }

        @Override
        public String toString() {
            return "Coin{" +
                    "symbol='" + symbol + '\'' +
                    ", name='" + name + '\'' +
                    ", iconRes=" + iconRes +
                    '}';
        }
    }
}
