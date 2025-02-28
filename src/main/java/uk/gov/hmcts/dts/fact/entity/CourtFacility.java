package uk.gov.hmcts.dts.fact.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "search_courtfacility")
@Getter
@Setter
@NoArgsConstructor
public class CourtFacility {

    @Id
    @SequenceGenerator(name = "seq-gen", sequenceName = "search_courtfacility_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq-gen")
    private Integer id;

    @OneToOne()
    @JoinColumn(name = "court_id")
    private Court court;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "facility_id")
    private Facility facility;

    public CourtFacility(final Court court, final Facility facility) {
        this.court = court;
        this.facility = facility;
    }
}
