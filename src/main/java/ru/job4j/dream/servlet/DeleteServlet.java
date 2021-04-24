package ru.job4j.dream.servlet;

import ru.job4j.dream.store.PsqlStore;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DeleteServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws
            ServletException, IOException {
        int idPhoto = Integer.parseInt(req.getParameter("photoID"));
        int idCan = Integer.parseInt(req.getParameter("canID"));
        int idCity = Integer.parseInt(req.getParameter("cityId"));
        PsqlStore.instOf().deleteCan(idCan);
        PsqlStore.instOf().deletePhoto(idPhoto);
        PsqlStore.instOf().deleteCity(idCity);
        resp.sendRedirect(req.getContextPath() + "/candidates.do");
    }
}
