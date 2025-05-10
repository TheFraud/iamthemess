package safevision.tech;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import safevision.tech.databinding.FragmentFirstBinding;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupNavigation();
    }

    private void setupNavigation() {
        binding.buttonFirst.setOnClickListener(v -> navigateTo(R.id.action_FirstFragment_to_SecondFragment));
    }

    private void navigateTo(int actionId) {
        try {
            NavHostFragment.findNavController(FirstFragment.this).navigate(actionId);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Erreur de navigation : " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
