package pro.sky.sockswarehouseapplication.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pro.sky.sockswarehouseapplication.model.transactions.Transactions;

import java.util.List;

@Repository
public class TransactionsDAO {

    private final SessionFactory sessionFactory;

    @Autowired
    public TransactionsDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Transactional(readOnly = true)
    public List<Transactions> index() {
        Session session = sessionFactory.getCurrentSession();
        return session.createQuery("select transactions from Transactions transactions", Transactions.class)
                .getResultList();
    }

    @Transactional
    public void save(Transactions transactions) {
        Session session = sessionFactory.getCurrentSession();
        session.save(transactions);
    }

    @Transactional
    public void delete(long id) {
        Session session = sessionFactory.getCurrentSession();
        session.remove(session.get(Transactions.class, id));
    }
}
