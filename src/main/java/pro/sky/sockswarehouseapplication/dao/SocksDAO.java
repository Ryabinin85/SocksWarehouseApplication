package pro.sky.sockswarehouseapplication.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pro.sky.sockswarehouseapplication.model.socks.Socks;

import java.util.List;

@Repository
public class SocksDAO {

    private final SessionFactory sessionFactory;

    @Autowired
    public SocksDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Transactional(readOnly = true)
    public List<Socks> index() {
        Session session = sessionFactory.getCurrentSession();
        return session.createQuery("select socks from Socks socks", Socks.class)
                .getResultList();
    }

    @Transactional
    public void save(Socks socks) {
        Session session = sessionFactory.getCurrentSession();
        session.save(socks);
    }

    @Transactional
    public void update(Socks socks, long id) {
        Session session = sessionFactory.getCurrentSession();
        Socks socksUpdated = session.get(Socks.class, id);
        socksUpdated.setQuantity(socks.getQuantity());

    }

    @Transactional
    public void delete(long id) {
        Session session = sessionFactory.getCurrentSession();
        session.remove(session.get(Socks.class, id));
    }
}
