package com.structkit.app

internal val beamReferences=mapOf(
    0 to "LQMdaRJCexE",1 to "dStn0_uEBqw",2 to "rapqMDfy3uU",3 to "dStn0_uEBqw",4 to "6ZJcbyTdFL0",
    5 to "ZoXvXYeJb0Q",6 to "PDvihxYq3Fs",7 to "-7hgh1HKdyQ",8 to "YX53NOP8uMo",9 to "-WGkgX-wu9E",
    10 to "Kbs_go3Xw_0",11 to "kEfN0uegPsY",12 to "OPSLdaMF-Dc",13 to "riMRo28-uLg",14 to "ZVOqzjjga04",
    15 to "5FCuu8bIvic",16 to "wWqCDfAuqxA",17 to "ZuPdkBLi85c",18 to "EprvaSYy0oM",19 to "ShBYi3U3VkU",20 to "Rx18gpzeqlM")

internal val beamPreparation=mapOf(
    0 to "Drill the incoming beam web. Shop-weld the fin plate to the support. Bolt through the fin and beam web on site. Leave the specified erection gap.",
    1 to "Square-cut the beam end; weld the flexible plate to the beam web. Drill the supporting flange. Keep flange clearances and bolt access.",
    2 to "Cope the upper flange where needed for the supporting flange. Drill the incoming web and the supporting web. Fit one cleat each side; the illustrated cleats are bolted on both legs.",
    3 to "Prepare the beam end and weld the flush plate to flanges and web. Drill the column flange. Add column stiffeners only where the design requires them.",
    4 to "Weld the extended plate to the beam flanges and web. Weld the triangular stiffeners between plate and beam flange. Fit transverse column stiffeners at the designed force-transfer levels.",
    5 to "Square-cut both beam ends and leave the specified splice gap. Drill matching flange and web cover plates. This is a separate conventional cover-plate arrangement, not a copy of the end-plate splice reference.",
    6 to "Prepare a stiffened seat and a top restraint angle. Fix the seat to the column and bear the bottom flange on it. Install the top restraint and specified beam bolts.",
    7 to "Cut and weld the haunch web and sloping bottom flange beneath the rafter. Weld the end plate to the rafter and haunch. Place column stiffeners at the force-transfer levels.",
    8 to "Prepare the two rafter ends to the roof pitch. Weld the haunches and matching apex end plates. Drill matching bolt groups and bolt the plates together.",
    9 to "Weld matching extended plates to the two beam ends. Weld the flange stiffeners to the plates and beam flanges. Drill matching bolt groups before site assembly.",
    10 to "Square-cut the hollow beam and weld its perimeter to the end plate. Use the designed extended bolt rows and column stiffeners. Keep access for tightening.",
    11 to "Weld a receiver plate to the hollow column face. Weld the mating end plate to the beam. Use the accessible projecting plate edges for the bolt rows; do not assume ordinary bolts can be tightened inside a closed hollow column.",
    12 to "Weld the cap plate to the column top. Drill matching holes in cap plate and beam bottom flange. Seat and bolt the beam; check local bearing and restraint.",
    13 to "Weld the base plate to the upper column. Drill the supporting beam flange. Add bearing stiffeners in the beam web as designed, then bolt the column plate down.",
    14 to "Set the connection plate to the actual beam elevations. Weld it to the incoming beam end and drill the primary web. Check bolt positions against primary flanges; unequal levels require a project-specific plate and force path.",
    15 to "Drill matching flange holes where the crossing beams meet. Bolt the lower runway beam top flange to the upper support beam bottom flange. Check fatigue, local flange bending and trolley clearance.",
    16 to "Weld the column cap/bearing plate. Provide girder bearing stiffeners at the support. Drill and bolt the bottom flange to the bearing plate; allow the specified movement and lateral restraint.",
    17 to "Cope the incoming upper flange and drill the beam web. Weld the projecting fin plate to the primary beam web. Bolt through the incoming web and fin plate.",
    18 to "Check that the incoming beam and end plate fit between the column flanges. Trim or cope only as detailed. Weld the plate to the beam web and bolt it to the drilled column web.",
    19 to "Weld the upper haunch web and sloping flange above the beam. Weld them and the beam end to the extended plate. Fit the designed column stiffeners opposite the concentrated forces.",
    20 to "Prepare stiffened brackets for both beam flanges, with matching drilled holes. Connect the brackets to the column flange and the beam flanges. Design the web shear attachment and column stiffeners as required."
)

internal val additionalBeamConnections=listOf(
    ConnectionDetail("Eaves haunch","Rafter to column",7,"A deepened rafter end connects through an extended end plate.","Haunch length/depth, roof pitch, plate and stiffener thicknesses, bolt rows, welds.","Moment and shear; haunch stability; panel zone; prying; welds; frame stiffness."),
    ConnectionDetail("Apex haunch","Rafter to rafter",8,"Paired rafter end plates with under-rafter haunches form the apex joint.","Roof pitch, haunch depth/length, bolt gauge and pitches, plate thickness, welds.","Rafter forces; bolt tension/shear; plate bending; haunch buckling; erection fit-up."),
    ConnectionDetail("End-plate splice","Beam to beam in line",9,"Matching extended end plates are welded to the two beam ends and bolted together.","Plate extensions, bolt rows, splice position, flange stiffeners and welds.","Moment and shear; prying; welds; bolt access; splice stiffness."),
    ConnectionDetail("Hollow beam end plate","RHS to I-column",10,"A hollow beam is perimeter-welded to an extended end plate.","RHS wall thickness; end plate; bolt rows; column stiffeners; perimeter weld.","RHS wall resistance; weld access; plate bending; column local resistance."),
    ConnectionDetail("Hollow-column receiver","I-beam to hollow column",11,"Projecting receiver and end plates provide externally accessible bolt rows.","Receiver projection; column wall; plates; bolt access; weld lengths.","Hollow-column face resistance; receiver bending; welds; bolt group; joint stiffness."),
    ConnectionDetail("Beam on cap plate","Beam over column",12,"The beam bottom flange bears on a column cap plate and is bolted through it.","Bearing length; cap-plate thickness; bolt positions; column top welds.","Bearing; local flange bending; web buckling; uplift and lateral restraint."),
    ConnectionDetail("Column over beam","Column on transfer beam",13,"An upper column base plate is fixed to the supporting beam top flange.","Base-plate dimensions; bolt positions; beam bearing stiffeners; welds.","Concentrated load; beam web bearing/buckling; flange bending; stability."),
    ConnectionDetail("Unequal-level beams","Offset beam ends",14,"An end plate connects beams with different flange elevations.","Actual level offset; plate height; bolt edge distances; primary-flange clearance.","Eccentricity; bolt group; primary web resistance; local bending; tying."),
    ConnectionDetail("Monorail suspension","Crossing beams",15,"The lower runway beam is suspended from an upper beam through their adjacent flanges.","Flange thicknesses; bolt gauge/pitch; contact; runway and trolley clearances.","Fatigue; local flange bending; bolt tension; dynamic effects; lateral stability."),
    ConnectionDetail("Crane girder bearing","Girder on column",16,"A girder bears over a cap plate with a stiffened web at the reaction.","Bearing length; stiffeners; cap plate; hold-down bolts; movement allowance.","Dynamic and fatigue loads; bearing; horizontal forces; lateral restraint."),
    ConnectionDetail("Beam-to-beam fin plate","Coped secondary beam",17,"A projecting plate welded to the primary web is bolted to the secondary web.","Cope depth/length/radius; fin plate; bolts; erection gap; welds.","Coped section resistance; bolt shear/bearing; fin bending; welds; rotation."),
    ConnectionDetail("Column-web end plate","Beam between column flanges",18,"A compact beam end plate bolts to the column web inside the flanges.","Clear width; flange trimming; plate and bolt positions; erection gap.","Column web resistance; bolt access; beam-end trimming; welds; tying."),
    ConnectionDetail("Upper haunch","Beam-to-column moment joint",19,"An upper haunch increases the depth of the beam end at the extended end plate.","Haunch length/depth; plate extension; welds; bolt rows; column stiffeners.","Moment and shear; haunch stability; plate bending; prying; panel zone."),
    ConnectionDetail("Stiffened flange brackets","Beam-to-column bracket joint",20,"Upper and lower stiffened brackets attach the beam flanges to the column.","Bracket thickness/projection; stiffener shape; bolt groups; flange clearances.","Flange-force transfer; bracket bending; prying; bolts; welds; column stiffeners.")
)
