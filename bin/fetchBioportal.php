#!/usr/bin/php
<?php
	$apiKey = "";
	$dirToStoreIn = "bioportal";
	if (file_exists($dirToStoreIn)) {
		shell_exec("rm -r ".$dirToStoreIn);
	}
	mkdir($dirToStoreIn);
	$ontologies = fetchOntologyList();
	
	fetchOntologies($ontologies);
	
	function fetchOntologyList() {
		global $apiKey;
		$url = "http://data.bioontology.org/ontologies?apikey=".$apiKey;
		return json_decode(file_get_contents($url), true);
	}
	
	function fetchOntologies($ontologies) {
		global $dirToStoreIn, $apiKey;
		//file_put_contents("Tmpfile.zip", fopen("http://someurl/file.zip", 'r'));
		foreach ($ontologies AS $ontology) {
			$id = $ontology["@id"];
			$targetFile = $dirToStoreIn."/".basename($id).".xml";
			//echo file_get_contents($id."/download?apikey=".$apiKey);
			//exit;
			//echo $targetFile;
			file_put_contents($targetFile, fopen($id."/download?apikey=".$apiKey, 'r'));
			//echo basename($id);
			//var_export($ontology);
			//exit;
		}
		
	} 
