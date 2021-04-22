package ru.job4j.dream.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

public class GreetingServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/plain");
        resp.setCharacterEncoding("UTF-8");
        String name = req.getParameter("name");
        PrintWriter writer = new PrintWriter(resp.getOutputStream());
        writer.println("Nice to meet you, " + name);
        writer.flush();
    }

    @Override
    protected void doPost(HttpServletRequest req,HttpServletResponse res)
        throws ServletException,IOException {
        JsonObject data = new Gson().fromJson(req.getReader(), JsonObject.class);
        String name = data.get("name").getAsString();
        String answer = "Nice to meet you, " + name;
        Map<String, String> options = new LinkedHashMap<>();
        options.put("answer", answer);
        String json = new Gson().toJson(options);
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        PrintWriter writer = new PrintWriter(res.getOutputStream());
        writer.write(json);
        writer.flush();
    }
}
