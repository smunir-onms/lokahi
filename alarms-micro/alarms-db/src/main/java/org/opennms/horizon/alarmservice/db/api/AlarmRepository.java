package org.opennms.horizon.alarmservice.db.api;

import javax.transaction.Transactional;
import org.opennms.horizon.alarmservice.db.impl.entity.Alarm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlarmRepository extends JpaRepository<Alarm, Integer> {
    @Transactional(Transactional.TxType.REQUIRED)

    public Alarm findByReductionKey(String reductionKey);
}
