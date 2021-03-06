var globs = {
	eventMounseOver : true
};

$(document).ready(function()
{	
	OnLoadInitConfigBackEnd();
	$("#film").attr("disabled", false);
	$("#search").attr("disabled", false);
	autoCompleteTitle();
	$("#panel_filter").buttonset();
	$("#panel_complete_type").buttonset();

	$("#panel_result").on("mouseenter", "img", function()
	{
		if (globs.eventMounseOver)
		{
			var id = $(this).attr("id");
			id = id.substr(id.indexOf("_") + 1);
			$("#thumb_" + id).hide();
			$("#play_" + id).show();
		}

	});

	$("#panel_result").on("mouseleave", "img", function()
	{
		if (globs.eventMounseOver)
		{
			var id = $(this).attr("id");
			id = id.substr(id.indexOf("_") + 1);
			$("#play_" + id).hide();
			$("#thumb_" + id).show();
		}
	});

	$("#panel_result").on("click", "img", function()
	{
		var id = $(this).attr("id");
		var idd = id.substr(id.indexOf("_") + 1);
		id = id.substr(0, id.indexOf("_"));
		var path = $(this).parent().parent().parent().find("#path").text();
		if (id == "play")
		{
			globs.eventMounseOver = false;
			$("#thumb_" + idd).hide();
			$("#play_" + idd).hide();
			$("#progress_" + idd).show();
			$.ajax({
				url : "playFilm.jsp",
				data : {
					path : path
				},
				type : "POST",
				dataType : "json",
				success : function(data)
				{
					$.fileDownload(data.url, {
						preparingMessageHtml : "We are preparing your report, please wait...",
						failMessageHtml : "There was a problem generating your report, please try again."
					});
					globs.eventMounseOver = true;
					$("#thumb_" + idd).show();
					$("#play_" + idd).hide();
					$("#progress_" + idd).hide();
				},
				error : function(result, status, err)
				{
					globs.eventMounseOver = true;
					$("#thumb_" + idd).show();
					$("#play_" + idd).hide();
					$("#progress_" + idd).hide();
					log("Error loading data");
					return;
				}
			});
		}
	});

	$("#search").click(function()
	{
		$(this).attr("disabled", true);
		GetResults();
	});

	$('#film').on('keypress', function(event)
	{
		if (event.which == '13')
		{
			$("#film").attr("disabled", true);
			$("#search").attr("disabled", true);
			GetResults();

		}
	});

});

function GetResults()
{
	$.ajax({
		url : "getFilm.jsp",
		data : {
			film : $("#film").val(),
			filter_query : $(".filters_group:checked").val(),
			filter_rgx : $(".filters_group_type:checked").val()
		},
		type : "POST",
		dataType : "json",
		success : buildResultLines
	});
}

function GetImage(id_name, mongo_id_image)
{
	$.ajax({
		url : "getImage.jsp",
		data : {
			image : mongo_id_image
		},
		type : "POST",
		dataType : "json",
		success : function(data)
		{
			$("#" + id_name).attr("src", "data:image/jpeg;base64," + data.value);
		}
	});

}

function OnLoadInitConfigBackEnd()
{
	$.ajax({
		url : "InitConfigBackEnd.jsp",
		data : {},
		type : "POST",
		dataType : "json",
		success : function(data){}
	});
}


function ImageExist(url)
{
	var img = new Image();
	img.src = url;
	return img.height != 0;
}

function StripImageName(name)
{
	var s1 = name.substr(name.indexOf("/") + 1);
	var s2 = s1.substr(0, s1.indexOf("."));
	return s2;
}

function buildResultLines(data)
{
	$("#panel_result").html("");
	for (var i = 0; i < data.values.length; i++)
	{
		var image_id = StripImageName(data.values[i].foto_mini);
		$("#panel_result").append(
				'<div id="line" class="shape wapper">' + '<div class="block1">' + '<img class ="thumb" src="img/ico_film.png" id="thumb_' + image_id
						+ '" alt="thumb_image"/>' + '<a href="#"><img class="opacy" src="img/video-play.png" id="play_' + image_id + '" alt="play_image"/></a>'
						+ '<img class ="progress" src="img/progress.gif" id="progress_' + image_id + '" alt="progress_image"/>' + '</div>'
						+ '<div class="block2">' + '<p class="p1">' + data.values[i].titulo + '</p>' + '<p class="p2">' + data.values[i].release_date
						+ '<br />' + data.values[i].generos + '<br />' + data.values[i].directores + '<br />' + data.values[i].actores + '<br />'
						+ '<span id="path">' + data.values[i].file_full_path + '</span>' + '</p>' + '</div>' + '<div class="block3">' + '<p class="p3">'
						+ data.values[i].sinopsis + '</p>' + '</div>' + '</div>');

		$("#" + "play_" + image_id).hide();
		$("#" + "progress_" + image_id).hide();
		GetImage("thumb_" + image_id, data.values[i].foto_mini);
	}
	$("#film").attr("disabled", false);
	$("#search").attr("disabled", false);
}

function autoCompleteTitle()
{
	$("#film").keypress(function()
	{
		$("#film").autocomplete({
			source : function(request, response)
			{
				$.ajax({
					url : "getFilms.jsp",
					data : {
						film : $("#film").val(),
						filter_query : $(".filters_group:checked").val(),
						filter_rgx : $(".filters_group_type:checked").val()
					},
					type : "POST",
					dataType : "json",
					success : function(data)
					{
						response($.map(data.values, function(item)
						{
							return {
								label : item,
								value : item
							};
						}));
					}
				});
			},
			select : function(event, ui)
			{
				if (ui.item)
					showFilm(ui.item);
			},
			open : function()
			{
				$(this).removeClass("ui-corner-all").addClass("ui-corner-top");
			},
			close : function()
			{
				$(this).removeClass("ui-corner-top").addClass("ui-corner-all");
			}
		});
	});
}

function showFilm(item)
{

}
