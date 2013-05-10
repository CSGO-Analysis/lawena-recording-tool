"Resource/UI/SpectatorGUIHealth.res"
{
	"PlayerStatusHealthImage"
	{
		"ControlName"	"ImagePanel"
		"fieldName"		"PlayerStatusHealthImage"
		"xpos"			"-8888"
		"ypos"			"-8888"
		"zpos"			"-1"
		"wide"			"18"
		"tall"			"18"
		"visible"		"1"
		"enabled"		"1"
		"scaleImage"	"1"	
	}		
	"PlayerStatusHealthImageBG"
	{
		"ControlName"	"ImagePanel"
		"fieldName"		"PlayerStatusHealthImageBG"
		"xpos"			"-8888"
		"ypos"			"-8888"
		"zpos"			"-2"
		"wide"			"22"
		"tall"			"22"
		"visible"		"1"
		"enabled"		"1"
		"image"			"../hud/health_bg"
		"scaleImage"	"1"	
	}	
	"BuildingStatusHealthImageBG"
	{
		"ControlName"	"ImagePanel"
		"fieldName"		"BuildingStatusHealthImageBG"
		"xpos"			"-8888"
		"ypos"			"-8888"
		"zpos"			"-1"
		"wide"			"28"
		"tall"			"28"
		"visible"		"0"
		"enabled"		"1"
		"image"			"../hud/health_equip_bg"
		"scaleImage"	"1"	
	}	
	"PlayerStatusHealthBonusImage"
	{
		"ControlName"	"ImagePanel"
		"fieldName"		"PlayerStatusHealthBonusImage"
		"xpos"			"12"
		"ypos"			"14"
		"zpos"			"-1"
		"wide"			"15"
		"tall"			"15"
		"visible"		"0"
		"enabled"		"1"
		"image"			"../hud/health_over_bg"
		"scaleImage"	"1"	
	}
	"PlayerStatusHealthValueTarget"
	{
		"ControlName"	"Label"
		"fieldName"		"PlayerStatusHealthValueTarget"
		"xpos"			"0"
		"ypos"			"10"
		"zpos"			"20"
		"wide"			"40"
		"tall"			"20"
		"visible"		"1"
		"enabled"		"1"
		"textAlignment"	"center"	
		"labeltext"		"%Health%"
		"font"			"M0refont24"
		"fgcolor_override"  "255 255 255 255"
	}
	"PlayerStatusHealthValueTargetShadow"
	{
		"ControlName"	"Label"
		"fieldName"		"PlayerStatusHealthValueTargetShadow"
		"xpos"			"1"
		"ypos"			"11"
		"zpos"			"20"
		"wide"			"40"
		"tall"			"20"
		"visible"		"1"
		"enabled"		"1"
		"textAlignment"	"center"	
		"labeltext"		"%Health%"
		"font"			"M0refont24"
		"fgcolor_override"  "0 0 0 255"
	}										
}
