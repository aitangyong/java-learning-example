package opensource.calcite;

import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.junit.Test;

public class ParseDemo {
    @Test
    public void t1() throws Exception {
        // 假设gdsInfo就是维表
        String sql = "select * from orders o join gdsInfo g on o.gdsId=g.gdsId";

        SqlParser.Config config = SqlParser.configBuilder().setLex(Lex.MYSQL).build();
        SqlParser sqlParser = SqlParser.create(sql, config);
        SqlSelect sqlSelect = (SqlSelect) sqlParser.parseStmt();

        SqlNode sqlFrom = sqlSelect.getFrom();
        boolean isSideJoin = false;
        String leftTable = "";
        String rightTable = "";
        String newName = ""; //临时表
        SqlJoin sqlJoin = null;

        // 解析join
        if (sqlFrom.getKind() == SqlKind.JOIN) {
            sqlJoin = (SqlJoin) sqlFrom;
            SqlNode left = sqlJoin.getLeft();
            SqlNode right = sqlJoin.getRight();
            isSideJoin = true;
            leftTable = parseTableName(left);
            rightTable = parseTableName(right);
        }

        //生成新的select
        if (isSideJoin) {
            newName = leftTable + "_" + rightTable;
            SqlParserPos pos = new SqlParserPos(0, 0);
            SqlIdentifier sqlIdentifier = new SqlIdentifier(newName, pos);
            sqlSelect.setFrom(sqlIdentifier);
        }
    }

    //解析表
    private static String parseTableName(SqlNode tbl) {
        if (tbl.getKind() == SqlKind.AS) {
            SqlBasicCall sqlBasicCall = (SqlBasicCall) tbl;
            return sqlBasicCall.operands[1].toString();
        }
        return ((SqlIdentifier) tbl).toString();
    }
}
