package com.edithdevstudio.edithfit.showcase

import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.records.metadata.DataOrigin
import androidx.health.connect.client.records.metadata.Device
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.units.Mass
import com.google.common.truth.Truth.assertThat
import java.time.Instant
import java.time.ZoneOffset
import org.junit.Test

/**
 * Curated excerpt based on the private EDITH Fit mapper tests.
 * It demonstrates the engineering requirement that normalized health records
 * retain source application and device provenance.
 */
class ProvenanceMappingTest {

    @Test
    fun `weight mapping preserves source and device provenance`() {
        val timestamp = Instant.parse("2026-08-28T12:00:00Z")
        val metadata = io.mockk.mockk<Metadata>(relaxed = true)

        io.mockk.every { metadata.id } returns "synthetic-health-record"
        io.mockk.every { metadata.dataOrigin } returns DataOrigin("com.sec.android.app.shealth")
        io.mockk.every { metadata.device } returns Device(
            manufacturer = "Samsung",
            model = "Galaxy Watch6",
            type = Device.TYPE_WATCH
        )

        val record = WeightRecord(
            weight = Mass.kilograms(75.5),
            time = timestamp,
            zoneOffset = ZoneOffset.ofHours(2),
            metadata = metadata
        )

        // In the private project DtoMappers converts the Health Connect record
        // into the normalized persistent DTO.
        val dto = DtoMappers.mapWeightRecord(record, "synthetic-user")

        assertThat(dto.value).isEqualTo(75.5)
        assertThat(dto.unit).isEqualTo("kg")
        assertThat(dto.sourcePackage).isEqualTo("com.sec.android.app.shealth")
        assertThat(dto.sourceDisplayName).isEqualTo("Samsung Health")
        assertThat(dto.deviceManufacturer).isEqualTo("Samsung")
        assertThat(dto.deviceModel).isEqualTo("Galaxy Watch6")
    }
}
