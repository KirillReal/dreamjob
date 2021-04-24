package ru.job4j.dream.servlet;

import org.json.JSONObject;
import ru.job4j.dream.store.PsqlStore;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

public class CityServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        PrintWriter writer = new PrintWriter(resp.getOutputStream());
        JSONObject json = new JSONObject();
        Collection<String> cities = PsqlStore.instOf().findAllCities();
        int i = 1;
        for (String city : cities) {
            json.put(String.valueOf(i++), city);
        }
        writer.println(json);
        writer.flush();
    }
}
