package com.usal.jorgeav.sportapp.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.data.DataBufferUtils;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLngBounds;
import com.usal.jorgeav.sportapp.R;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Adaptador para la lista de sugerencias de ciudades. Utiliza la interfaz {@link Filterable}
 * para filtrar las busquedas. Para las busquedas utiliza Google Places API
 *
 * @see
 * <a href= "https://developers.google.com/android/reference/com/google/android/gms/location/places/GeoDataApi">
 *     Google Places API
 * </a>
 */
public class PlaceAutocompleteAdapter
        extends ArrayAdapter<AutocompletePrediction> implements Filterable {
    /**
     * Nombre de la clase
     */
    private static final String TAG = PlaceAutocompleteAdapter.class.getSimpleName();
    /**
     * Estilo de texto para la celda
     */
    private static final CharacterStyle STYLE_BOLD = new StyleSpan(Typeface.BOLD);

    /**
     * Colección de resultados una vez efectuada la busqueda
     */
    private ArrayList<AutocompletePrediction> mResultList;
    /**
     * Objeto GoogleApiClient necesario para utilizar Google Places API
     *
     * @see
     * <a href= "https://developers.google.com/android/reference/com/google/android/gms/common/api/GoogleApiClient">
     *     GoogleApiClient
     * </a>
     * @see
     * <a href= "https://developers.google.com/android/reference/com/google/android/gms/location/places/GeoDataApi">
     *     Google Places API
     * </a>
     */
    private GoogleApiClient mGoogleApiClient;
    /**
     * Limites en coordenadas sobre los que efectuar la busqueda de lugares
     */
    private LatLngBounds mBounds;
    /**
     * Filtro usado para restringir la busqueda a solo el nombre de ciudades
     */
    private AutocompleteFilter mPlaceFilter;

    /**
     *
     * @param context Contexto de la Actividad que aloja el adaptador
     * @param googleApiClient necesario para acceder a las funciones de la API
     * @param bounds limites sobre los que efectuar la busqueda
     * @param filter filtro de lugares para restringir la busqueda
     */
    public PlaceAutocompleteAdapter(Context context, GoogleApiClient googleApiClient,
                                    LatLngBounds bounds, AutocompleteFilter filter) {
        super(context, android.R.layout.simple_expandable_list_item_2, android.R.id.text1);
        this.mGoogleApiClient = googleApiClient;
        this.mBounds = bounds;
        this.mPlaceFilter = filter;
    }

    /**
     *
     * @return el tamaño de la lista de ciudades
     */
    @Override
    public int getCount() {
        return mResultList.size();
    }

    /**
     *
     * @param position posicion dentro de la lista
     * @return el elemento situado en esa posicion
     */
    @Override
    public AutocompletePrediction getItem(int position) {
        return mResultList.get(position);
    }

    /**
     * Rellena los elementos de la celda con los datos de la ciudad de la posicion indicada
     *
     * @param position posicion de la celda
     * @param convertView View de la celda
     * @param parent View del padre donde se situa la celda
     * @return la celda con los datos emplazados
     */
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View row = super.getView(position, convertView, parent);
        AutocompletePrediction item = getItem(position);

        if (item != null) {
            TextView textView1 = (TextView) row.findViewById(android.R.id.text1);
            TextView textView2 = (TextView) row.findViewById(android.R.id.text2);
            textView1.setText(item.getPrimaryText(STYLE_BOLD));
            textView2.setText(item.getSecondaryText(STYLE_BOLD));
        }

        return row;
    }

    /**
     * Crea el {@link Filter} que realiza las  busquedas.
     * <p>
     * El filtro creado utiliza una lista para almacenar los resultados de la consulta a Google
     * en {@link Filter#performFiltering(CharSequence)} y luego los almacena
     * en {@link #mResultList} en el metodo
     * {@link Filter#publishResults(CharSequence, Filter.FilterResults)}
     * <p>
     * @return el filtro creado
     */
    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();

                // We need a separate list to store the results, since
                // this run asynchronously.
                ArrayList<AutocompletePrediction> filterData = new ArrayList<>();

                // Skip the autocomplete query if no constraints are given.
                if (constraint != null) {
                    // Query the autocomplete API for the (constraint) search string.
                    filterData = getAutocomplete(constraint);
                }

                results.values = filterData;
                if (filterData != null) results.count = filterData.size();
                else results.count = 0;

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    // The API returned at least one result, update the data.
                    mResultList = (ArrayList<AutocompletePrediction>) results.values;
                    notifyDataSetChanged();
                } else {
                    // The API did not return any results, invalidate the data set.
                    notifyDataSetInvalidated();
                }
            }

            @Override
            public CharSequence convertResultToString(Object resultValue) {
                // Override this method to display a readable result in the AutocompleteTextView
                // when clicked.
                if (resultValue instanceof AutocompletePrediction) {
                    return ((AutocompletePrediction) resultValue).getFullText(null);
                } else {
                    return super.convertResultToString(resultValue);
                }
            }
        };
    }

    /**
     * Metodo encargado de realizar la consulta asíncrona a Google Places API con la cadena de
     * texto introducida por el usuario
     *
     * <p>Utiliza el metodo GeoDataApi.getAutocompletePredictions() para hacer las consultas.
     *
     * <p>A continuacion, espera por el resultado, comprueba el codigo de error y cierra el
     * AutocompletePredictionBuffer por el que recibia la respuesta.
     *
     * <p>Si el codigo es correcto devuelve una lista con los resultados que coinciden con la
     * busqueda
     *
     * @param constraint cadena de texto con la que se realiza la busqueda
     *
     * @return ArrayList con los resultados de la busqueda
     *
     * @see
     * <a href= "https://developers.google.com/android/reference/com/google/android/gms/location/places/GeoDataApi">
     *     Google Places API
     * </a>
     * @see
     * <a href= "https://developers.google.com/android/reference/com/google/android/gms/location/places/GeoDataApi.html#getAutocompletePredictions(com.google.android.gms.common.api.GoogleApiClient,%20java.lang.String,%20com.google.android.gms.maps.model.LatLngBounds,%20com.google.android.gms.location.places.AutocompleteFilter)">
     *     GeoDataApi.getAutocompletePredictions()
     * </a>
     * @see
     * <a href= "https://developers.google.com/android/reference/com/google/android/gms/location/places/AutocompletePredictionBuffer">
     *     AutocompletePredictionBuffer
     * </a>
     */
    private ArrayList<AutocompletePrediction> getAutocomplete(CharSequence constraint) {
        if (mGoogleApiClient.isConnected()) {
            // Submit the query to the autocomplete API and retrieve a PendingResult that will
            // contain the results when the query completes.
            PendingResult<AutocompletePredictionBuffer> results = Places.GeoDataApi
                    .getAutocompletePredictions(mGoogleApiClient, constraint.toString(),
                            mBounds, mPlaceFilter);

            // This method should have been called off the main UI thread. Block and wait for 15s
            AutocompletePredictionBuffer autocompletePredictions = results
                    .await(15, TimeUnit.SECONDS);

            final Status status = autocompletePredictions.getStatus();
            if (!status.isSuccess()) {
                if (status.getStatusCode() == CommonStatusCodes.NETWORK_ERROR)
                    Toast.makeText(getContext(), R.string.error_check_conn, Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getContext(), R.string.toast_error_autocomplete_city, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error getting autocomplete prediction API call: " + status.toString());
                autocompletePredictions.release();
                return null;
            }

            // Freeze the results immutable representation to be stored safely
            // and release the AutocompletePredictionBuffer
            return DataBufferUtils.freezeAndClose(autocompletePredictions);
        }
        Log.e(TAG, "Google API client is not connected for autocomplete query.");
        return null;
    }
}
