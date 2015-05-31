package com.github.kuros.random.jpa.random;

import com.github.kuros.random.jpa.testUtil.entity.Employee;
import com.github.kuros.random.jpa.random.generator.Generator;
import com.github.kuros.random.jpa.random.generator.RandomGenerator;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Created by Kumar Rohit on 5/14/15.
 */
public class RandomizeImplTest {
    @Test
    public void shouldRandmoizeEmployeeClass() throws InstantiationException, IllegalAccessException {
        final RandomGenerator randomFactory = RandomGenerator.newInstance(Generator.newInstance());
        final Randomize randomize = RandomizeImpl.newInstance(null, randomFactory);
        final Employee randomEmployee = randomize.createRandom(Employee.class);

        assertNotNull(randomEmployee);
//        assertNotNull(randomEmployee.getEmployeeId());
//        assertNotNull(randomEmployee.getPersonId());
//        assertNotNull(randomEmployee.getSalary());
    }
}
