package com.nithra.nithraresume.pdf

import com.nithra.nithraresume.data.model.ResumeFormat
import com.nithra.nithraresume.data.model.SectionChild1
import com.nithra.nithraresume.data.model.SectionChild2
import com.nithra.nithraresume.data.model.SectionChild3
import com.nithra.nithraresume.data.model.SectionChild4
import com.nithra.nithraresume.data.model.SectionChild5
import com.nithra.nithraresume.data.model.SectionChild6
import com.nithra.nithraresume.data.model.SectionChild7
import com.nithra.nithraresume.data.model.SectionChild8
import com.nithra.nithraresume.data.model.SectionHeadAdded
import com.nithra.nithraresume.data.model.UserProfile

/**
 * All data needed to generate a PDF resume.
 * Sections and addons must be pre-filtered (enabled only) and sorted by indexPosition.
 */
data class ResumePdfData(
    val profile: UserProfile,
    val format: ResumeFormat,
    val sections: List<SectionHeadAdded>,
    val addons: List<SectionHeadAdded>,
    val sc1ByHeadId: Map<Int, SectionChild1>,
    val sc2sByHeadId: Map<Int, List<SectionChild2>>,
    val sc3sByHeadId: Map<Int, List<SectionChild3>>,
    val sc4ByHeadId: Map<Int, SectionChild4>,
    val sc5ByHeadId: Map<Int, SectionChild5>,
    val sc6sByHeadId: Map<Int, List<SectionChild6>>,
    val sc7sByHeadId: Map<Int, List<SectionChild7>>,
    val sc8ByHeadId: Map<Int, SectionChild8>
)
// update 111
