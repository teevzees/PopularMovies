package com.jefftiensivu.popularmovies.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.PrimaryKey;
import net.simonvt.schematic.annotation.References;

/**
 * Created by jeff on 11/11/2015.
 */
public interface TrailerColumns {
    @DataType(DataType.Type.INTEGER)
    @PrimaryKey @AutoIncrement
    String ID = "_id";

    @DataType(DataType.Type.TEXT)
    String PARENT_TABLE = "parent_table";

    @DataType(DataType.Type.INTEGER)
    @References(table = PARENT_TABLE, column = PopMoviesColumns.TMDB_ID)
    String PARENT_TMDB_ID = "parent_tmdb_id";

    @DataType(DataType.Type.TEXT)
    String NAME = "name";

    @DataType(DataType.Type.TEXT)
    String SIZE = "size";

    @DataType(DataType.Type.TEXT)
    String SOURCE = "source";

    @DataType(DataType.Type.TEXT)
    String TYPE = "type";

}