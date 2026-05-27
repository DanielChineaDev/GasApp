package com.bpo.gasapp.data.repository

import com.bpo.gasapp.data.local.VehicleDao
import com.bpo.gasapp.data.local.entity.VehicleEntity
import com.bpo.gasapp.domain.model.FuelType
import com.bpo.gasapp.domain.model.Vehicle
import com.bpo.gasapp.domain.repository.VehicleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VehicleRepositoryImpl @Inject constructor(
    private val vehicleDao: VehicleDao
) : VehicleRepository {

    override fun observeVehicles(): Flow<List<Vehicle>> =
        vehicleDao.observeAll().map { list -> list.map { it.toDomain() } }.flowOn(Dispatchers.Default)

    override suspend fun getById(id: Long): Vehicle? = vehicleDao.getById(id)?.toDomain()

    override suspend fun add(name: String, fuel: FuelType, consumption: Double): Long =
        vehicleDao.insert(VehicleEntity(name = name, fuel = fuel.name, consumption = consumption))

    override suspend fun update(vehicle: Vehicle) =
        vehicleDao.update(VehicleEntity(vehicle.id, vehicle.name, vehicle.fuel.name, vehicle.consumption))

    override suspend fun delete(vehicle: Vehicle) =
        vehicleDao.delete(VehicleEntity(vehicle.id, vehicle.name, vehicle.fuel.name, vehicle.consumption))

    private fun VehicleEntity.toDomain(): Vehicle = Vehicle(
        id = id,
        name = name,
        fuel = runCatching { FuelType.valueOf(fuel) }.getOrDefault(FuelType.GASOLINA_95),
        consumption = consumption
    )
}
