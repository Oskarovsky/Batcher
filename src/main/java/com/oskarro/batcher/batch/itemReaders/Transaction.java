package com.oskarro.batcher.batch.itemReaders;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    private Long transactionId;
    private BigDecimal price;
}
