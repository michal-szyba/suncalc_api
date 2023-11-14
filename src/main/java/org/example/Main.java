package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Main {
    public static void main(String[] args) {
        try (Workbook workbook = new XSSFWorkbook(); FileOutputStream fileOut = new FileOutputStream("suncalc.xlsx")) {
            String urlBase = "https://api.sunrisesunset.io/json?lat=50.081561&lng=19.922429&timezone=UTC+2:00&date=2020-";
            Sheet sheet = workbook.createSheet("sunrise_sunset");
            String url = "";
            int rowNumber = 1;
            for (int month = 1; month < 13; month++) {
                int maxDay = getMonthLength(2020, month);
                for (int day = 1; day <= maxDay; day++) {
                    String formattedDate = String.format("%02d-%02d", month, day);
                    url = urlBase + formattedDate;
                    String[] resultArr = connect(url);
                    System.out.println(String.format("MONTH: %s DAY: %s", month, day));
                    if (resultArr != null) {
                        Row row = sheet.createRow(rowNumber);
                        Cell dateCell = row.createCell(0);
                        dateCell.setCellValue(formattedDate);
                        for (int cellNumber = 1; cellNumber <= resultArr.length; cellNumber++) {
                            Cell cell = row.createCell(cellNumber);
                            cell.setCellValue(resultArr[cellNumber - 1]);
                        }
                        rowNumber++;
                    }
                }
            }
            workbook.write(fileOut);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String[] connect(String url) {
        try {
            URL apiUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                String jsonResponse = response.toString();
                connection.disconnect();
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(jsonResponse);
                String sunset = jsonNode.get("results").get("sunset").asText();
                String sunrise = jsonNode.get("results").get("sunrise").asText();
                String dayLength = jsonNode.get("results").get("day_length").asText();
                String[] resArr = new String[]{sunrise, sunset, dayLength};
                System.out.println(resArr[0]);
                System.out.println(resArr[1]);
                System.out.println(resArr[2]);
                return resArr;
            } else {
                System.out.println("Response error: "  + responseCode);
                connection.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int getMonthLength(int year, int month) {
        switch (month) {
            case 4:
            case 6:
            case 9:
            case 11:
                return 30;
            case 2:
                if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) {
                    return 29;
                } else {
                    return 28;
                }
            default:
                return 31;
        }
    }
}
