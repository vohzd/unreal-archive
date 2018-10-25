<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<title>${title}</title>
	<style>
		@import url('https://fonts.googleapis.com/css?family=Abel|Anton');

		body {
			padding: 0;
			margin: 0;

			font-family: 'Abel', sans-serif;
			font-size: 1em;
		}

		.page {
			max-width: 1024px;
			margin-left: auto;
			margin-right: auto;
		}

		header {
			height: 100px;
		}

		section.header {
			position: relative;
			height: 150px;
			box-shadow: inset 0 0 100px 0 rgba(0, 0, 0, 0.75);
			background: gray center;
			background-size: cover;
		}

		section.header h1 {
			font-family: 'Anton', sans-serif;
			font-size: 1.8em;
			position: absolute;
			bottom: 0;
			color: white;
			text-shadow: -2px -2px 0 #000, 2px -2px 0 #000, -2px 2px 0 #000, 2px 2px 0 #000;
			padding: 0;
			margin: 10px 20px;
		}

		.biglist ul {
			list-style: none;
			padding: 0;

			display: grid;
			grid-template-columns: 33.3%;
			grid-template-areas: 'block block block';
			grid-gap: 10px;
		}

		.biglist ul li {
			grid-area: 'block';
			position: relative;
			height: 100px;
			box-shadow: inset 0 0 80px 0 rgba(0, 0, 0, 0.75);
			background: gray center;
			background-size: cover;

			font-size: 1.5em;
			font-weight: bold;
			color: white;
		}

		.biglist ul li a {
			text-decoration: none;
			color: white;
			text-shadow: -2px -2px 0 #000, 2px -2px 0 #000, -2px 2px 0 #000, 2px 2px 0 #000;

			position: absolute;
			top: 0;
			bottom: 0;
			left: 0;
			right: 0;
			padding: 10px;
		}

		.biglist ul li a:hover {
			background-color: rgba(255, 255, 255, 0.5);
		}

		.biglist ul li .meta {
			color: rgba(255, 255, 255, 0.5);
			font-size: 2em;

			position: absolute;
			bottom: -10px;
			right: 0;
		}

		.maplist nav {
			display: grid;
			grid-gap: 2px;
			grid-auto-flow: column;
			font-size: 1.2em;
			font-weight: bold;
			margin: 10px 0;
		}

		.maplist nav.letters a {
			background-color: #dee3e9;
			border: 1px solid #4D7A97;
			padding: 5px;
			margin: 0 2px;
			text-decoration: none;
			text-align: center;
		}

		.maplist nav.pages a {
			background-color: #dee3e9;
			border: 1px solid #4D7A97;
			padding: 5px;
			margin: 0 2px;
			text-decoration: none;
			text-align: center;
		}

		.maplist nav.letters a.active {
			background-color: white;
			border-bottom-width: 4px;
		}

		.maplist nav.pages a.active {
			background-color: white;
			border-bottom-width: 4px;
		}

		table.maps {
			width: 100%;
		}

		table.maps thead {
			background-color: #4D7A97;
			color: white;
		}

		table.maps tr.odd {
			background-color: #d0d9e0;
		}

		table.maps td, table.maps th {
			padding: 5px 0;
		}
	</style>
</head>

<body>

<div class="page">
	<header>
	</header>
