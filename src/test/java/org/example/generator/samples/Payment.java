package org.example.generator.samples;

import org.example.generator.Generatable;
import org.example.generator.samples.explicit.CardPayment;

@Generatable(implementations = CardPayment.class)
public interface Payment {
}
