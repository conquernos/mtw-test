package com.hc.mtw;

import org.conqueror.common.utils.db.DBConnector;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;


public class PicoTrainingSetBuilder {

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver");

        String jdbcUrl = "jdbc:mysql://localhost:3306/mtw?characterEncoding=UTF-8&serverTimezone=UTC";
        String jdbcUser = "mtw";
        String jdbcPassword = "mtw!@#123";

        String selectQuery = "SELECT url" +
            ", domain" +
            ", tag" +
            ", text" +
            ", page_width" +
            ", page_height" +
            ", width" +
            ", height" +
            ", x" +
            ", y" +
            ", r" +
            ", g" +
            ", b" +
            ", font_size" +
            ", font_style_number" +
            ", font_family" +
            ", font_weight" +
            ", word_count" +
            ", char_count" +
            ", font_norm" +
            ", size_norm" +
            ", color_diff" +
            ", location_num" +
            ", number_ratio" +
            ", special_char_ratio" +
            ", avg_width" +
            ", avg_height" +
            ", avg_x" +
            ", avg_y" +
            ", max_width" +
            ", max_height" +
            ", max_x" +
            ", max_y" +
            " FROM features3";

        Set<String> keys = new java.util.HashSet<>();

        try (DBConnector connector = new DBConnector(jdbcUrl, jdbcUser, jdbcPassword)) {
            try (ResultSet result = connector.select(selectQuery)) {
                while (result.next()) {
                    String site = result.getString("domain");
                    String url = result.getString("url");

                    String tag = result.getString("tag");
                    String text = result.getString("text");
                    int pageWidth = result.getInt("page_width");
                    int pageHeight = result.getInt("page_height");
                    int width = result.getInt("width");
//                    int avgWidth = result.getInt("avg_width");
                    int maxWidth = result.getInt("max_width");
                    int height = result.getInt("height");
//                    int avgHeight = result.getInt("avg_height");
                    int maxHeight = result.getInt("max_height");
                    int x = result.getInt("x");
//                    int avgX = result.getInt("avg_x");
                    int maxX = result.getInt("max_x");
                    int y = result.getInt("y");
//                    int avgY = result.getInt("avg_y");
                    int maxY = result.getInt("max_y");
//                    int r = result.getInt("r");
//                    int g = result.getInt("g");
//                    int b = result.getInt("b");
                    double fontSize = result.getDouble("font_size");
                    int fontStyleNumber = result.getInt("font_style_number");
                    int fontWeight = result.getInt("font_weight");
                    int wordCount = result.getInt("word_count");
                    int charCount = result.getInt("char_count");
                    double fontNorm = result.getDouble("font_norm");
                    double sizeNorm = result.getDouble("size_norm");
                    double colorDiff = result.getDouble("color_diff");
                    int locationNumber = result.getInt("location_num");
                    double numberRatio = result.getDouble("number_ratio");
                    double specialCharRatio = result.getDouble("special_char_ratio");

                    if (!keys.add(url + tag + text)) continue;

                    double[] array = new double[]{
                        tag.equals("product_name") ? 0
                            : tag.equals("product_price") ? 1
                            : tag.equals("product_desc") ? 2
                            : 3
                        , (double) width / maxWidth
                        , (double) height / maxHeight
                        , (double) width / pageWidth
                        , (double) height / pageHeight
                        , (double) x / maxX
                        , (double) y / maxY
                        , (double) x / pageWidth
                        , (double) y / pageHeight
//                        , r / 255.d
//                        , g / 255.d
//                        , b / 255.d
                        , Math.min((fontSize / 36.d), 1.d)
                        , Math.min((fontStyleNumber / 3.d), 1.d)
                        , Math.min((fontWeight / 1000.d), 1.d)
                        , Math.min((wordCount / 30.d), 1.d)
                        , Math.min((charCount / 100.d), 1.d)
                        , fontNorm
                        , sizeNorm
                        , colorDiff / 442.d
                        , locationNumber / 6.d
                        , numberRatio
                        , specialCharRatio
//                        , text.matches("((£|€|\\$|₩|USD|AUD|EUR|KRW|원|달러|유로)+.*|.*(£|€|\\$|₩|USD|AUD|EUR|KRW|원|달러|유로)+)") ? 1 : 0
                        , text.matches(".*(£|€|\\$|₩|USD|AUD|EUR|KRW|원|달러|유로)+.*")? 1 : 0
                    };

                    System.out.printf("%s;%s;%.0f,%.8f,%.8f,%.8f,%.8f,%.8f,%.8f,%.8f,%.8f,%.8f,%.8f,%.8f,%.8f,%.8f,%.8f,%.8f,%.8f,%.8f,%.8f,%.8f,%.8f\n"
                        , site
                        , url
                        , array[0], array[1], array[2], array[3], array[4], array[5], array[6], array[7], array[8], array[9], array[10]
                        , array[11], array[12], array[13], array[14], array[15], array[16], array[17], array[18], array[19], array[20]
                    );

//                    System.out.printf("%s;%s;%.0f,%.8f,%.8f,%.8f,%.8f,%.8f,%.8f,%.8f,%.8f,%.8f,%.8f,%.8f,%.8f,%.8f,%.8f,%.8f,%.8f\n"
//                        , site
//                        , url
//                        , array[0], array[1], array[2], array[3], array[4], array[5], array[6], array[7], array[8], array[9], array[10]
//                        , array[11], array[12], array[13], array[14], array[15], array[16]
//                    );
                }

            }
        }

    }

}
