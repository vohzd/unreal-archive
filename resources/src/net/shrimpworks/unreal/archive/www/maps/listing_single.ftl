<#include "_header.ftl">

	<section class="header">
		<h1>
		${gametype.game.name} / ${gametype.name}
		</h1>
	</section>
	<article>
		<table class="maplist">
			<thead>
			<tr>
				<th>Map</th>
				<th>Title</th>
				<th>Author</th>
				<th>Players</th>
			</tr>
			</thead>
			<tbody>
				<#list maps as m>
				<tr>
					<td><a href="${relUrl(gametype.path, m.path + ".html")}">${m.map.name}</a></td>
					<td>${m.map.title}</td>
					<td>${m.map.author}</td>
					<td>${m.map.playerCount}</td>
				</tr>
				</#list>
			</tbody>
		</table>
	</article>

<#include "_footer.ftl">