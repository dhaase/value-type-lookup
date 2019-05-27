package eu.dirk.haase.value;

import eu.dirk.haase.eu.dirk.haase.domain.GeldBetrag;
import eu.dirk.haase.eu.dirk.haase.domain.MyGeldBetrag;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(BlockJUnit4ClassRunner.class)
public class GeldBetragTest {


    @Test
    public void test() {
        // Given
        GeldBetrag geldBetrag = new MyGeldBetrag("456");
        GeldBetrag nonGeldBetrag0 = new MyGeldBetrag();
        // When
        GeldBetrag nonGeldBetrag1 = geldBetrag.valueOf(null);
        GeldBetrag nonGeldBetrag2 = geldBetrag.valueOf(null);
        GeldBetrag nonGeldBetrag3 = geldBetrag.valueOf(null);
        // Then
        assertThat(nonGeldBetrag1).isInstanceOf(GeldBetrag.class);
        assertThat(nonGeldBetrag1).isEqualTo(nonGeldBetrag0);
        assertThat(nonGeldBetrag1).isEqualTo(nonGeldBetrag2);
        assertThat(nonGeldBetrag1).isEqualTo(nonGeldBetrag3);
    }

}
