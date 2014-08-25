package ch.quazz.caverna.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

import ch.quazz.caverna.score.Tile;
import ch.quazz.caverna.score.Token;
import ch.quazz.caverna.score.PlayerScore;

public class PlayerScoreTable {

    private static final String TableName = "player_score";

    private static final class ColumnName {
        private static final String Id = "id";

        private static final String PlayerId = "player_id";
        private static final String GameId = "game_id";

        private static final String Tiles = "tiles";
    }

    private static final Map<Token, String> TokenColumns =
            new HashMap<Token, String>(){
                {
                    put(Token.Dwarfs, "dwarfs");
                    put(Token.Dwellings, "dwellings");

                    put(Token.Dogs, "dogs");
                    put(Token.Sheep, "sheep");
                    put(Token.Donkeys, "donkeys");
                    put(Token.Boars, "boars");
                    put(Token.Cattle, "cattle");

                    put(Token.Grains, "grains");
                    put(Token.Vegetables, "vegetables");

                    put(Token.Rubies, "rubies");
                    put(Token.Gold, "gold");
                    put(Token.BeggingMarkers, "begging_markers");

                    put(Token.SmallPastures, "small_pastures");
                    put(Token.LargePastures, "large_pastures");

                    put(Token.OreMines, "ore_mines");
                    put(Token.RubyMines, "ruby_mines");

                    put(Token.UnusedSpace, "unused_space");

                    put(Token.Wood, "wood");
                    put(Token.Stone, "stone");
                    put(Token.Ore, "ore");
                }
    };

    static final String createTableSql() {
        String sql = "CREATE TABLE " + TableName;

        sql += " ( " + ColumnName.Id + " INTEGER PRIMARY KEY";

        sql += " , " + ColumnName.PlayerId + " INTEGER";
        sql += " , " + ColumnName.GameId + " INTEGER";

        for (String column : TokenColumns.values()) {
            sql += " , " + column + " INTEGER";
        }

        sql += " , " + ColumnName.Tiles + " TEXT";
        sql += ")";

        return sql;
    }

    static final String deleteTableSql() {
        return "DROP TABLE IF EXISTS " + TableName;
    }

    private final CavernaDbHelper dbHelper;

    public PlayerScoreTable(CavernaDbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public void save(PlayerScore playerScore) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(TableName, null, null);

        ContentValues values = new ContentValues();

        for (Map.Entry<Token, String> column : TokenColumns.entrySet()) {
            values.put(column.getValue(), playerScore.getCount(column.getKey()));
        }

        JSONArray furnishings = new JSONArray();
        for (Tile tile : Tile.values()) {
            if(playerScore.has(tile)) {
                furnishings.put(tile);
            }
        }
        values.put(ColumnName.Tiles, furnishings.toString());

        db.insert(TableName, null, values);
    }

    public void load(PlayerScore playerScore) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(TableName, null, null, null, null, null, null, "1");

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();

            for (Map.Entry<Token, String> column : TokenColumns.entrySet()) {
                playerScore.setCount(column.getKey(), cursor.getInt(cursor.getColumnIndex(column.getValue())));
            }

            try {
                JSONArray read = new JSONArray(cursor.getString(cursor.getColumnIndex(ColumnName.Tiles)));

                for (int i = 0; i < read.length(); i++) {
                    Tile tile = Tile.valueOf(read.getString(i));
                    playerScore.set(tile);
                }
            } catch(JSONException exception) {
                erase();
            }
        }
    }

    public void erase() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(TableName, null, null);
    }
}