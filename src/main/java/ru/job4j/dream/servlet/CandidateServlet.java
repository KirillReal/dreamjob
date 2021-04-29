package ru.job4j.dream.servlet;

import ru.job4j.dream.model.Candidate;
import ru.job4j.dream.store.PsqlStore;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CandidateServlet extends HttpServlet {

    private static final Map<Integer, Integer> CITY_MAPPING = new HashMap<>();

    static {
        CITY_MAPPING.put(1, 5);
        CITY_MAPPING.put(2, 7);
        CITY_MAPPING.put(3, 8);
        CITY_MAPPING.put(4, 9);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Collection<Candidate> candidates = PsqlStore.instOf().findAllCandidates();
        req.setAttribute("candidates", candidates);
        Map<Integer, String> map = new HashMap<>();
        candidates.stream().map(Candidate::getCityId).
                forEach(id -> map.put(id, PsqlStore.instOf().findByIdCity(id)));
        req.setAttribute("user", req.getSession().getAttribute("user"));
        req.setAttribute("cities", map);
        req.getRequestDispatcher("candidates.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        Candidate candidate = new Candidate(Integer.parseInt(req.getParameter("id")),
                req.getParameter("name"),
                0, CITY_MAPPING.get(Integer.parseInt(req.getParameter("cityValue"))));
        PsqlStore.instOf().save(candidate);
        resp.sendRedirect(req.getContextPath() + "/upload" + "?candidateId=" + candidate.getId());
        //resp.sendRedirect(req.getContextPath() + "/candidates.do");
    }
}
