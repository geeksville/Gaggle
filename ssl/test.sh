#!/bin/bash

if [ -x vali-xgg ]; then
	./vali-xgg sample_tracks/flight_good_sig.igc
	OK=$?
        ./vali-xgg sample_tracks/flight_bad_sig.igc
	BAD=$?

	if [  $OK == 0  -a  $BAD != 0  ]; then
		echo "Looks fine"
	else
		echo "Looks bad. ok=$OK (should be 0), bad=$BAD (should be !=0)"
	fi
else
	echo "Please compile verify first :)"
fi

