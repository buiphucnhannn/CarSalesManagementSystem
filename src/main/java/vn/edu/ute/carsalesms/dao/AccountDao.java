package vn.edu.ute.carsalesms.dao;

import vn.edu.ute.carsalesms.model.entity.Account;

import java.util.Optional;

public interface AccountDao {

    Optional<Account> findByUsername(String username);

    Account save(Account account);
}

